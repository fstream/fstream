/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist.spark;

import static com.google.common.base.Strings.repeat;
import static java.util.concurrent.TimeUnit.SECONDS;
import io.fstream.core.config.KafkaProperties;
import io.fstream.core.model.topic.Topic;
import io.fstream.persist.config.PersistProperties;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import kafka.serializer.StringDecoder;
import lombok.Cleanup;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import scala.Tuple2;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.AbstractExecutionThreadService;

/**
 * Service responsible for persisting to the long-term HDFS backing store.
 * <p>
 * This class is <em>not</em> thread-safe.
 */
@Slf4j
@Service
@Profile("spark")
public class SparkService extends AbstractExecutionThreadService {

  /**
   * Configuration.
   */
  @Value("${spark.workDir}")
  private String workDir;
  @Value("${spark.batchInterval}")
  private long batchInterval;

  @Autowired
  private PersistProperties properties;
  @Autowired
  private KafkaProperties kafkaProperties;

  /**
   * Dependencies.
   */
  @Autowired
  private JavaSparkContext sparkContext;
  @Autowired
  private FileSystem fileSystem;

  @PostConstruct
  public void init() throws Exception {
    log.info("Initializing HDFS persist job...");
    startAsync();
    log.info("Finished initializing HDFS persist job");
  }

  @PreDestroy
  public void destroy() throws Exception {
    log.info("Destroying '{}'...");
    stopAsync();
  }

  @Override
  public void run() throws IOException {
    clean();

    @Cleanup
    val streamingContext = createStreamingContext();

    for (val topic : properties.getTopics()) {
      create(topic, streamingContext);
    }

    start(streamingContext);
  }

  private void clean() throws IOException {
    log.info("Deleting work dir '{}'...", workDir);
    fileSystem.delete(new Path(workDir), true);
  }

  private void create(Topic topic, JavaStreamingContext streamingContext) {
    log.info(repeat("-", 100));
    log.info("Creating DStream for topic '{}'...", topic);
    log.info(repeat("-", 100));

    val sqlContext = createSQLContext();
    val kafkaStream = createKafkaStream(topic, streamingContext);

    kafkaStream.foreachRDD((rdd, time) -> {
      persist(topic, sqlContext, rdd, time);

      return null;
    });
  }

  private void persist(Topic topic, SQLContext sqlContext, JavaPairRDD<String, String> rdd, Time time) {
    val messages = rdd.map(Tuple2::_2);
    log.info("[{}] Partition count: {}, message count: {}", topic, messages.partitions().size(), messages.count());

    val combined = messages.coalesce(1); // Combine into single partition

    val schemaRdd = sqlContext.read().json(combined);
    val parquetFile = getParquetFileName(topic, time);
    schemaRdd.write().parquet(parquetFile);
  }

  private void start(JavaStreamingContext streamingContext) {
    log.info("Starting streams...");
    streamingContext.start();

    log.info("Awaiting shutdown...");
    streamingContext.awaitTermination();
  }

  private SQLContext createSQLContext() {
    return new SQLContext(sparkContext);
  }

  private JavaStreamingContext createStreamingContext() {
    val batchDuration = new Duration(SECONDS.toMillis(batchInterval));

    log.info("Creating streaming context with batch duration {}", batchDuration);
    return new JavaStreamingContext(sparkContext, batchDuration);
  }

  /**
   * @see https://spark.apache.org/docs/1.3.1/streaming-kafka-integration.html
   */
  private JavaPairReceiverInputDStream<String, String> createKafkaStream(Topic topic,
      JavaStreamingContext streamingContext) {
    log.info("Reading from topic: {}", topic.getId());
    val keyTypeClass = String.class;
    val valueTypeClass = String.class;
    val keyDecoderClass = StringDecoder.class;
    val valueDecoderClass = StringDecoder.class;
    val kafkaParams = kafkaProperties.getConsumerProperties();
    val partitions = ImmutableMap.of(topic.getId(), 1);
    val storageLevel = StorageLevel.MEMORY_AND_DISK_SER_2();

    return KafkaUtils.createStream(streamingContext, keyTypeClass, valueTypeClass, keyDecoderClass, valueDecoderClass,
        kafkaParams, partitions, storageLevel);
  }

  private String getParquetFileName(Topic topic, Time time) {
    return workDir + "/" + topic.getId() + "/" + topic.getId() + "-" + time.milliseconds();
  }

}

/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist.service;

import static com.google.common.base.Strings.repeat;
import static java.util.concurrent.TimeUnit.SECONDS;
import io.fstream.core.config.KafkaProperties;
import io.fstream.core.model.topic.Topic;
import io.fstream.persist.config.PersistProperties;

import java.io.IOException;
import java.util.Collections;

import javax.annotation.PostConstruct;

import kafka.serializer.StringDecoder;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import scala.Tuple2;

import com.google.common.collect.ImmutableMap;

/**
 * Service responsible for persisting to the long-term HDFS backing store.
 * <p>
 * This class is <em>not</em> thread-safe.
 */
@Slf4j
@Service
@Profile("spark")
public class SparkService {

  /**
   * Configuration.
   */
  @Value("${spark.workDir}")
  private String workDir;
  @Value("${spark.interval}")
  private long interval;

  @Autowired
  private PersistProperties persistProperties;
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
  public void run() throws IOException {
    // TODO: This should not happen in production!
    cleanStorage();

    try (val streamingContext = createStreamingContext()) {
      createStreams(streamingContext);
      startStream(streamingContext);
    }
  }

  private void cleanStorage() throws IOException {
    log.info("Deleting work dir '{}'...", workDir);
    fileSystem.delete(new Path(workDir), true);
  }

  private void createStreams(final org.apache.spark.streaming.api.java.JavaStreamingContext streamingContext) {
    for (val topic : persistProperties.getTopics()) {
      createStream(topic, streamingContext);
    }
  }

  private void createStream(Topic topic, JavaStreamingContext streamingContext) {
    log.info(repeat("-", 100));
    log.info("Creating DStream for topic '{}'...", topic);
    log.info(repeat("-", 100));

    val sqlContext = createSQLContext();
    val kafkaStream = createKafkaStream(topic, streamingContext);

    kafkaStream.foreachRDD((rdd, time) -> {
      persistInterval(topic, sqlContext, rdd, time);
      return null;
    });
  }

  private void persistInterval(Topic topic, SQLContext sqlContext, JavaPairRDD<String, String> rdd, Time time) {
    val messages = rdd.map(Tuple2::_2);
    log.info("[{}] Partition count: {}", topic, messages.partitions().size());
    log.info("[{}] Message count: {}", topic, messages.count());

    // Combine into single partition
    val combined = messages.coalesce(1);

    val schemaRdd = sqlContext.read().json(combined);
    val parquetFile = getParquetFileName(topic, time);
    schemaRdd.write().parquet(parquetFile);
  }

  private void startStream(JavaStreamingContext streamingContext) {
    log.info("Starting streams...");
    streamingContext.start();

    log.info("Awaiting shutdown...");
    streamingContext.awaitTermination();
  }

  private SQLContext createSQLContext() {
    return new SQLContext(sparkContext);
  }

  private JavaStreamingContext createStreamingContext() {
    log.info("Creating streaming context with a duration {}s", interval);
    val duration = new Duration(SECONDS.toMillis(interval));

    return new JavaStreamingContext(sparkContext, duration);
  }

  /**
   * @see http://spark.apache.org/docs/latest/streaming-kafka-integration.html#approach-2-direct-approach-no-receivers
   */
  private JavaPairInputDStream<String, String> createKafkaStream(Topic topic,
      JavaStreamingContext streamingContext) {
    log.info("Reading from topic: {}", topic.getId());
    val topics = Collections.singleton(topic.getId());

    val keyTypeClass = String.class;
    val valueTypeClass = String.class;
    val keyDecoderClass = StringDecoder.class;
    val valueDecoderClass = StringDecoder.class;
    val kafkaParams = ImmutableMap.<String, String> builder()
        .putAll(kafkaProperties.getProducerProperties())
        .putAll(kafkaProperties.getConsumerProperties())
        .build();

    return KafkaUtils.createDirectStream(streamingContext, keyTypeClass, valueTypeClass, keyDecoderClass,
        valueDecoderClass, kafkaParams, topics);
  }

  private String getParquetFileName(Topic topic, Time time) {
    return workDir + "/" + topic.getId() + "/" + topic.getId() + "-" + time.milliseconds();
  }

}

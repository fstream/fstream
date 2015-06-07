/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist.service;

import static io.fstream.core.model.topic.Topic.TOQ;
import static java.util.concurrent.TimeUnit.SECONDS;
import io.fstream.persist.config.PersistProperties.KafkaProperties;

import java.io.IOException;

import javax.annotation.PostConstruct;

import kafka.serializer.StringDecoder;
import lombok.Cleanup;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
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
    clean();

    @Cleanup
    val streamingContext = createStreamingContext();

    prepare(streamingContext);
    execute(streamingContext);
  }

  private void clean() throws IOException {
    log.info("Deleting work dir '{}'...", workDir);
    fileSystem.delete(new Path(workDir), true);
  }

  private void prepare(JavaStreamingContext streamingContext) {
    val sqlContext = createSQLContext();

    val kafkaStream = createKafkaStream(streamingContext);
    kafkaStream.foreachRDD((rdd, time) -> {
      val schemaRdd = sqlContext.jsonRDD(rdd.map(Tuple2::_2));
      schemaRdd.saveAsParquetFile(workDir + "/data-" + time.milliseconds());

      return null;
    });
  }

  private void execute(final org.apache.spark.streaming.api.java.JavaStreamingContext streamingContext) {
    streamingContext.start();
    streamingContext.awaitTermination();
  }

  private SQLContext createSQLContext() {
    return new SQLContext(sparkContext);
  }

  private JavaStreamingContext createStreamingContext() {
    val duration = new Duration(SECONDS.toMillis(interval));

    return new JavaStreamingContext(sparkContext, duration);
  }

  /**
   * @see https://spark.apache.org/docs/1.3.1/streaming-kafka-integration.html
   */
  private JavaPairReceiverInputDStream<String, String> createKafkaStream(JavaStreamingContext streamingContext) {
    val topic = TOQ.getId();
    val keyTypeClass = String.class;
    val valueTypeClass = String.class;
    val keyDecoderClass = StringDecoder.class;
    val valueDecoderClass = StringDecoder.class;
    val kafkaParams = kafkaProperties.getConsumerProperties();
    val partitions = ImmutableMap.of(topic, 1);
    val storageLevel = StorageLevel.MEMORY_AND_DISK_SER_2();

    return KafkaUtils.createStream(streamingContext, keyTypeClass, valueTypeClass, keyDecoderClass, valueDecoderClass,
        kafkaParams, partitions, storageLevel);
  }

}

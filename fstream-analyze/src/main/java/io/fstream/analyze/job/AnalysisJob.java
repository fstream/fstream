/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.analyze.job;

import static com.google.common.base.Strings.repeat;
import static java.util.concurrent.TimeUnit.SECONDS;
import io.fstream.analyze.config.AnalyzeProperties;
import io.fstream.analyze.kafka.KafkaProducer;
import io.fstream.analyze.kafka.KafkaProducerObjectPool;
import io.fstream.core.config.KafkaProperties;
import io.fstream.core.model.topic.Topic;

import java.io.IOException;

import javax.annotation.PostConstruct;

import kafka.serializer.StringDecoder;
import lombok.Cleanup;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.pool2.ObjectPool;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.AbstractExecutionThreadService;

/**
 * Service responsible for persisting to the long-term HDFS backing store.
 * <p>
 * This class is <em>not</em> thread-safe.
 */
@Slf4j
public abstract class AnalysisJob extends AbstractExecutionThreadService {

  /**
   * Configuration.
   */
  @Value("${spark.interval}")
  private long interval;

  @Autowired
  private AnalyzeProperties properties;
  @Autowired
  private KafkaProperties kafkaProperties;

  /**
   * Dependencies.
   */
  @Autowired
  private JavaSparkContext sparkContext;

  @PostConstruct
  public void init() throws Exception {
    log.info("Initializing analytics job...");
    startAsync();
    log.info("Finished initializing analytics job");
  }

  @Override
  protected void run() throws IOException {
    @Cleanup
    val streamingContext = createStreamingContext();

    createStreams(streamingContext);

    startStreams(streamingContext);
  }

  private void createStreams(JavaStreamingContext streamingContext) {
    for (val topic : properties.getTopics()) {
      createStream(topic, streamingContext);
    }
  }

  private void createStream(Topic topic, JavaStreamingContext streamingContext) {
    log.info(repeat("-", 100));
    log.info("Creating DStream for topic '{}'...", topic);
    log.info(repeat("-", 100));

    // Setup
    val sqlContext = createSQLContext();
    val kafkaStream = createKafkaStream(topic, streamingContext);
    val pool = createProducerPool(streamingContext);

    // Define
    kafkaStream.foreachRDD((rdd, time) -> {
      analyzeBatch(rdd, time, topic, pool, sqlContext);
      return null;
    });
  }

  /**
   * Template method to analyze the current {@code rdd} batch produced at {@code time}.
   * 
   * @param rdd the current RDD
   * @param time the current batch interval time
   * @param topic the topic being consumed
   * @param pool the Kafka producer pool
   * @param sqlContext
   */
  protected abstract void analyzeBatch(JavaPairRDD<String, String> rdd, Time time, Topic topic,
      Broadcast<ObjectPool<KafkaProducer>> pool, SQLContext sqlContext);

  private void startStreams(JavaStreamingContext streamingContext) {
    log.info("Starting streams...");
    streamingContext.start();

    log.info("Awaiting shutdown...");
    streamingContext.awaitTermination();
  }

  private JavaStreamingContext createStreamingContext() {
    val duration = new Duration(SECONDS.toMillis(interval));

    log.info("Creating streaming context at {}", duration);
    return new JavaStreamingContext(sparkContext, duration);
  }

  private SQLContext createSQLContext() {
    return new SQLContext(sparkContext);
  }

  /**
   * @see https://spark.apache.org/docs/1.4.1/streaming-kafka-integration.html
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

  private Broadcast<ObjectPool<KafkaProducer>> createProducerPool(JavaStreamingContext streamingContext) {
    val pool = new KafkaProducerObjectPool(kafkaProperties.getProducerProperties());

    return streamingContext.sparkContext().broadcast(pool);
  }

}

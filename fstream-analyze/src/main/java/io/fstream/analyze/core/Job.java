/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.analyze.core;

import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.google.common.base.Strings.repeat;
import static com.google.common.collect.Maps.newHashMap;
import static io.fstream.analyze.util.SerializableComparator.serialize;
import static java.util.stream.Collectors.toMap;
import io.fstream.core.model.event.MetricEvent;
import io.fstream.core.model.topic.Topic;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kafka.serializer.StringDecoder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.joda.time.DateTime;

import scala.Tuple2;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * Represents streaming analytical job.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class Job {

  /** The topics to read from. */
  @NonNull
  protected final Set<Topic> topics;

  /** Handle to context the job is running in. */
  @NonNull
  protected JobContext jobContext;

  public void register() {
    log.info(repeat("-", 100));
    log.info("Creating DStream from topics '{}'...", topics);
    log.info(repeat("-", 100));

    // Setup the input to the job
    val kafkaStream = createKafkaStream();

    // Plan the job behavior
    plan(kafkaStream);
  }

  /**
   * Template method
   */
  protected abstract void plan(JavaPairReceiverInputDStream<String, String> kafkaStream);

  /**
   * @see https://spark.apache.org/docs/1.4.1/streaming-kafka-integration.html
   */
  private JavaPairReceiverInputDStream<String, String> createKafkaStream() {
    // Short-hands
    val keyTypeClass = String.class;
    val valueTypeClass = String.class;
    val keyDecoderClass = StringDecoder.class;
    val valueDecoderClass = StringDecoder.class;
    val storageLevel = StorageLevel.MEMORY_AND_DISK_SER_2();

    // Resolve
    val kafkaParams = resolveKafkaParams();
    val partitions = resolveTopicPartitions();

    return KafkaUtils.createStream(jobContext.getStreamingContext(),
        keyTypeClass, valueTypeClass, keyDecoderClass, valueDecoderClass, kafkaParams, partitions, storageLevel);
  }

  private Map<String, Integer> resolveTopicPartitions() {
    return topics.stream().collect(toMap(Topic::getId, (x) -> 1));
  }

  private Map<String, String> resolveKafkaParams() {
    val consumerProperties = jobContext.getKafka().getConsumerProperties();
    val overrideGroupId = resolveKafkaConsumerGroupId(consumerProperties);

    log.info("Creating kafka params with group id: {}", overrideGroupId);
    val kafkaParams = newHashMap(consumerProperties);
    kafkaParams.put(ConsumerConfig.GROUP_ID_CONFIG, overrideGroupId);
    return kafkaParams;
  }

  private String resolveKafkaConsumerGroupId(Map<String, String> consumerProperties) {
    // Ensure Kafka consumers from different jobs are namespaced and therefore isolated
    val groupId = consumerProperties.get(ConsumerConfig.GROUP_ID_CONFIG);
    val jobId = UPPER_CAMEL.to(LOWER_HYPHEN, getClass().getSimpleName());

    return groupId + "-" + jobId;
  }

  /**
   * Factory methods - functions.
   */

  protected static <T extends Comparable<T>> Comparator<Tuple2<String, T>> valueDescending() {
    return serialize((a, b) -> a._2.compareTo(b._2));
  }

  /**
   * Factory methods.
   */

  protected static Set<Topic> topics(@NonNull Topic... topics) {
    return ImmutableSet.copyOf(topics);
  }

  protected static <T, U> Tuple2<T, U> pair(T t, U u) {
    return new Tuple2<>(t, u);
  }

  protected static <T> List<T> list(T t1, T t2) {
    return ImmutableList.<T> of(t1, t2);
  }

  protected static Map<String, Object> record(String k1, Object v1, String k2, Object v2) {
    return ImmutableMap.of(k1, v1, k2, v2);
  }

  protected static MetricEvent metricEvent(int id, Time time, List<? extends Tuple2<?, ?>> tuples) {
    val data = Lists.newArrayListWithCapacity(tuples.size());
    for (val tuple : tuples) {
      // Will be available downstream for display
      val record = record("userId", tuple._1, "value", tuple._2);
      data.add(record);
    }

    return new MetricEvent(new DateTime(time.milliseconds()), id, data);
  }

}

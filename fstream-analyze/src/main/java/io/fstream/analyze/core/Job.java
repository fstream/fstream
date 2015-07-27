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
import static java.util.stream.Collectors.toMap;
import io.fstream.core.model.topic.Topic;

import java.util.Map;
import java.util.Set;

import kafka.serializer.StringDecoder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.kafka.KafkaUtils;

import com.google.common.collect.Maps;

/**
 * Represents streaming analytical job.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class Job {

  /**
   * Configuration
   */
  @NonNull
  protected final Set<Topic> topics;

  /**
   * Dependencies.
   */
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
    val consumerProperties = jobContext.getKafkaProperties().getConsumerProperties();

    // Ensure Kafka consumers from different jobs are namespaced and therefore isolated
    val groupIdKey = "group.id";
    val groupId = consumerProperties.get(groupIdKey);
    val overrideGroupId = groupId + "-" + UPPER_CAMEL.to(LOWER_HYPHEN, getClass().getSimpleName());

    log.info("Creating kafka params with group id: {}", overrideGroupId);
    val kafkaParams = Maps.newHashMap(consumerProperties);
    kafkaParams.put(groupIdKey, overrideGroupId);
    return kafkaParams;
  }

}

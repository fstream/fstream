/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.analyze.core;

import io.fstream.analyze.kafka.KafkaProducer;
import io.fstream.core.config.KafkaProperties;
import lombok.NonNull;
import lombok.Value;

import org.apache.commons.pool2.ObjectPool;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

/**
 * Execution context of a {@code Job}.
 */
@Value
public class JobContext {

  /**
   * Configuration.
   */
  @NonNull
  KafkaProperties kafkaProperties;

  /**
   * Sub-contexts;
   */
  @NonNull
  JavaStreamingContext streamingContext;
  @NonNull
  SQLContext sqlContext;

  /**
   * Producers.
   */
  @NonNull
  Broadcast<ObjectPool<KafkaProducer>> pool;

}
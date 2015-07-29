/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.analyze.core;

import static java.util.concurrent.TimeUnit.SECONDS;
import io.fstream.analyze.config.AnalyzeProperties;
import io.fstream.analyze.kafka.KafkaProducer;
import io.fstream.analyze.kafka.KafkaProducerObjectPool;
import io.fstream.core.config.KafkaProperties;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.pool2.ObjectPool;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory for creating the shared {@link JobContext}.
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JobContextFactory {

  /**
   * Configuration.
   */
  @NonNull
  private final AnalyzeProperties analyze;
  @NonNull
  private final KafkaProperties kafka;

  /**
   * Dependencies.
   */
  @NonNull
  private final JavaSparkContext sparkContext;

  public JobContext create() {
    val streamingContext = createStreamingContext();
    val sqlContext = createSQLContext();
    val pool = createProducerPool(streamingContext);

    return new JobContext(kafka, streamingContext, sqlContext, pool);
  }

  private JavaStreamingContext createStreamingContext() {
    val batchDuration = new Duration(SECONDS.toMillis(analyze.getBatchInterval()));

    log.info("Creating streaming context with batch duration {}", batchDuration);
    val streamingContext = new JavaStreamingContext(sparkContext, batchDuration);
    streamingContext.checkpoint(analyze.getCheckpointDir());

    return streamingContext;
  }

  private SQLContext createSQLContext() {
    return new SQLContext(sparkContext);
  }

  private Broadcast<ObjectPool<KafkaProducer>> createProducerPool(JavaStreamingContext streamingContext) {
    val pool = new KafkaProducerObjectPool(kafka.getProducerProperties());

    return sparkContext.broadcast(pool);
  }

}

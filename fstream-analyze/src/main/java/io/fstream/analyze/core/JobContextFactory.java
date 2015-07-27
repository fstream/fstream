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
import io.fstream.analyze.kafka.KafkaProducer;
import io.fstream.analyze.kafka.KafkaProducerObjectPool;
import io.fstream.core.config.KafkaProperties;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.pool2.ObjectPool;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JobContextFactory {

  /**
   * Configuration.
   */
  @Value("${spark.interval}")
  private long interval;
  @Autowired
  private KafkaProperties kafkaProperties;

  /**
   * Dependencies.
   */
  @Autowired
  private JavaSparkContext sparkContext;

  public JobContext create() {
    val streamingContext = createStreamingContext();
    val sqlContext = createSQLContext();
    val pool = createProducerPool(streamingContext);

    return new JobContext(kafkaProperties, streamingContext, sqlContext, pool);
  }

  private JavaStreamingContext createStreamingContext() {
    val batchDuration = new Duration(SECONDS.toMillis(interval));

    log.info("Creating streaming context with batch duration {}", batchDuration);
    val streamingContext = new JavaStreamingContext(sparkContext, batchDuration);
    streamingContext.checkpoint("/tmp/fstream/checkpoint");

    return streamingContext;
  }

  private SQLContext createSQLContext() {
    return new SQLContext(sparkContext);
  }

  private Broadcast<ObjectPool<KafkaProducer>> createProducerPool(JavaStreamingContext streamingContext) {
    val pool = new KafkaProducerObjectPool(kafkaProperties.getProducerProperties());

    return sparkContext.broadcast(pool);
  }

}

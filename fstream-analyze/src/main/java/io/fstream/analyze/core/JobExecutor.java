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
import io.fstream.analyze.job.EventsJob;
import io.fstream.analyze.job.TopUserValueJob;
import io.fstream.analyze.kafka.KafkaProducer;
import io.fstream.analyze.kafka.KafkaProducerObjectPool;
import io.fstream.core.config.KafkaProperties;

import java.io.IOException;

import javax.annotation.PostConstruct;

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

import com.google.common.util.concurrent.AbstractExecutionThreadService;

/**
 * Executes {@link Job}s against a shared {@link JavaStreamingContext} with a provided {@link JobContext}.
 * <p>
 * Runs asynchronously in its own thread.
 */
@Slf4j
@Component
public class JobExecutor extends AbstractExecutionThreadService {

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

  @PostConstruct
  public void init() throws Exception {
    log.info("Initializing analytics job...");
    startAsync();
    log.info("Finished initializing analytics job");
  }

  @Override
  protected void run() throws IOException {
    try (val streamingContext = createStreamingContext()) {
      createJobs(streamingContext);
      startJobs(streamingContext);
    }
  }

  private void createJobs(JavaStreamingContext streamingContext) {
    log.info("Creating jobs...");
    val jobContext = createJobContext(streamingContext);

    // Registered jobs
    new TopUserValueJob(jobContext).execute(streamingContext);
    new EventsJob(jobContext).execute(streamingContext);
  }

  private void startJobs(JavaStreamingContext streamingContext) {
    log.info("Starting streams...");
    streamingContext.start();

    log.info("Awaiting shutdown...");
    streamingContext.awaitTermination();
  }

  private JobContext createJobContext(JavaStreamingContext streamingContext) {
    val sqlContext = createSQLContext();
    val pool = createProducerPool(streamingContext);

    return new JobContext(kafkaProperties, sqlContext, pool);
  }

  private JavaStreamingContext createStreamingContext() {
    val duration = new Duration(SECONDS.toMillis(interval));

    log.info("Creating streaming context at {}", duration);
    val streamingContext = new JavaStreamingContext(sparkContext, duration);
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

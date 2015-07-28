/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.analyze.job;

import static io.fstream.analyze.util.SerializableComparator.serialize;
import static io.fstream.core.model.topic.Topic.METRICS;
import io.fstream.analyze.core.Job;
import io.fstream.analyze.core.JobContext;
import io.fstream.analyze.kafka.KafkaProducer;
import io.fstream.core.model.event.Event;
import io.fstream.core.model.topic.Topic;

import java.util.Comparator;
import java.util.Set;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.pool2.ObjectPool;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.springframework.stereotype.Component;

import scala.Tuple2;

/**
 * Calculates a running total of all user / order values.
 */
@Slf4j
@Component
public abstract class TopUserJob extends Job {

  /**
   * Output metric identifier.
   */
  private final int metricId;

  /**
   * Top N.
   */
  private final int n;

  public TopUserJob(int metricId, Set<Topic> topics, int n, JobContext jobContext) {
    super(topics, jobContext);
    this.metricId = metricId;
    this.n = n;
  }

  protected <T extends Comparable<T>> void analyzeBatches(JavaPairDStream<String, T> calculation) {
    // Closure safety
    val metricId = this.metricId;
    val topics = this.topics;
    val n = this.n;
    val pool = jobContext.getPool();

    // Sort and top
    calculation.foreachRDD((rdd, time) -> {
      log.info("[{}:{}] RDD partition count: {}, RDD element count: {}",
          metricId, topics, rdd.partitions().size(), rdd.count());

      analyzeBatch(rdd, time, metricId, n, pool);
      return null;
    });
  }

  @SneakyThrows
  protected static <T extends Comparable<T>> void analyzeBatch(JavaPairRDD<String, T> rdd, Time time,
      int metricId, int n, Broadcast<ObjectPool<KafkaProducer>> pool) {
    // Find top N by value descending
    val tuples = rdd.top(n, valueDescending());

    // We use a pool here to amortize the cost of the Kafka socket connections over the entire job.
    val producer = pool.getValue().borrowObject();
    try {
      val metric = metricEvent(metricId, time, tuples);
      producer.send(METRICS, metric);
    } finally {
      pool.getValue().returnObject(producer);
    }
  }

  /**
   * Utilities.
   */

  @SuppressWarnings("unchecked")
  protected <T extends Event> Function<Event, T> castEvent(Class<T> t) {
    return event -> (T) event;
  }

  protected static <T extends Comparable<T>> Comparator<Tuple2<String, T>> valueDescending() {
    return serialize((a, b) -> a._2.compareTo(b._2));
  }

}

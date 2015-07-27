/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.analyze.job;

import static io.fstream.analyze.util.Functions.computeIntegerRunningSum;
import static io.fstream.analyze.util.Functions.parseOrder;
import static io.fstream.analyze.util.Functions.sumIntegerReducer;
import static io.fstream.analyze.util.SerializableComparator.serialize;
import static io.fstream.core.model.topic.Topic.METRICS;
import static io.fstream.core.model.topic.Topic.ORDERS;
import io.fstream.analyze.core.Job;
import io.fstream.analyze.core.JobContext;
import io.fstream.analyze.kafka.KafkaProducer;
import io.fstream.core.model.event.MetricEvent;
import io.fstream.core.model.topic.Topic;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.pool2.ObjectPool;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import scala.Tuple2;

import com.google.common.collect.Lists;

/**
 * Calculates a running total of all user / order values.
 */
@Slf4j
@Component
public class TopUserOrdersJob extends Job {

  /**
   * The metric id.
   */
  private static final int ID = 12;

  /**
   * Top N.
   */
  private final int n;

  @Autowired
  public TopUserOrdersJob(JobContext jobContext, @Value("${analyze.n}") int n) {
    super(topics(ORDERS), jobContext);
    this.n = n;
  }

  @Override
  protected void plan(JavaPairReceiverInputDStream<String, String> kafkaStream) {
    analyzeStream(kafkaStream, topics, n, jobContext.getPool());
  }

  private static void analyzeStream(JavaPairReceiverInputDStream<String, String> kafkaStream,
      Set<Topic> topics, int n, Broadcast<ObjectPool<KafkaProducer>> pool) {
    log.info("[{}] Order count: {}", topics, kafkaStream.count());

    // Get order amounts by user
    val userOrderAmounts =
        kafkaStream
            .map(parseOrder())
            .mapToPair(order -> new Tuple2<>(order.getUserId(), order.getAmount()))
            .reduceByKey(sumIntegerReducer())
            .updateStateByKey(computeIntegerRunningSum());

    // Sort and top
    userOrderAmounts.foreachRDD((rdd, time) -> {
      log.info("[{}] Partition count: {}, user count: {}", topics, rdd.partitions().size(), rdd.count());
      analyzeBatch(rdd, time, n, pool);
      return null;
    });
  }

  @SneakyThrows
  private static void analyzeBatch(JavaPairRDD<String, Integer> rdd, Time time, int n,
      Broadcast<ObjectPool<KafkaProducer>> pool) {
    // Find top N by value descending
    val tuples = rdd.top(n, userValueDescending());

    // We use a pool here to amortize the cost of the Kafka socket connections over the entire job.
    val producer = pool.getValue().borrowObject();
    try {
      val metric = createMetricEvent(time, tuples);

      log.info("Sending metric: {}...", metric);
      producer.send(METRICS, metric);
    } finally {
      pool.getValue().returnObject(producer);
    }
  }

  private static Comparator<Tuple2<String, Integer>> userValueDescending() {
    return serialize((a, b) -> a._2.compareTo(b._2));
  }

  private static MetricEvent createMetricEvent(Time time, List<? extends Tuple2<?, ?>> tuples) {
    val data = Lists.newArrayListWithCapacity(tuples.size());
    for (val tuple : tuples) {
      // Will be available downstream for display
      val record = record("userId", tuple._1, "value", tuple._2);
      data.add(record);
    }

    return metric(time, ID, data);
  }

}

/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.analyze.job;

import static io.fstream.analyze.util.Functions.compueFloatRunningSum;
import static io.fstream.analyze.util.Functions.parseOrder;
import static io.fstream.analyze.util.Functions.sumFloatReducer;
import static io.fstream.analyze.util.SerializableComparator.serialize;
import static io.fstream.core.model.topic.Topic.METRICS;
import static io.fstream.core.model.topic.Topic.ORDERS;
import io.fstream.analyze.core.Job;
import io.fstream.analyze.core.JobContext;
import io.fstream.analyze.kafka.KafkaProducer;
import io.fstream.core.model.event.MetricEvent;
import io.fstream.core.model.event.Order;
import io.fstream.core.model.topic.Topic;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.pool2.ObjectPool;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import scala.Tuple2;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * Calculates a running total of all user / order values.
 */
@Slf4j
@Component
public class TopUserValueJob extends Job {

  /**
   * Top N.
   */
  private static final int N = 20;

  @Autowired
  public TopUserValueJob(JobContext jobContext) {
    super(ImmutableSet.of(ORDERS), jobContext);
  }

  @Override
  protected void plan(JavaPairReceiverInputDStream<String, String> kafkaStream) {
    analyzeStream(kafkaStream, jobContext.getPool(), topics);
  }

  private static void analyzeStream(JavaPairReceiverInputDStream<String, String> kafkaStream,
      Broadcast<ObjectPool<KafkaProducer>> pool, Set<Topic> topics) {
    log.info("[{}] Order count: {}", topics, kafkaStream.count());

    // Define
    val aggregatedUserAmounts =
        kafkaStream
            .map(parseOrder())
            .mapToPair(pairUserIdValue())
            .reduceByKey(sumFloatReducer())
            .updateStateByKey(compueFloatRunningSum());

    aggregatedUserAmounts.foreachRDD((rdd, time) -> {
      log.info("[{}] Partition count: {}, order count: {}", topics, rdd.partitions().size(), rdd.count());
      analyzeBatch(rdd, time, pool);
      return null;
    });
  }

  @SneakyThrows
  private static void analyzeBatch(JavaPairRDD<String, Float> rdd, Time time, Broadcast<ObjectPool<KafkaProducer>> pool) {
    // Find top N by value descending
    val tuples = rdd.top(N, userValueDescending());

    val producer = pool.getValue().borrowObject();
    try {
      val metric = createMetricEvent(time, tuples);

      log.info("Sending metric: {}...", metric.getData());
      producer.send(METRICS, metric);
    } finally {
      pool.getValue().returnObject(producer);
    }
  }

  private static MetricEvent createMetricEvent(Time time, List<? extends Tuple2<?, ?>> tuples) {
    val data = Lists.newArrayListWithCapacity(tuples.size());
    for (val tuple : tuples) {
      data.add(ImmutableMap.of("userId", tuple._1, "value", tuple._2));
    }

    return new MetricEvent(new DateTime(time.milliseconds()), "topNUserValues".hashCode(), data);
  }

  private static Comparator<Tuple2<String, Float>> userValueDescending() {
    return serialize((x, y) -> x._2.compareTo(y._2));
  }

  public static PairFunction<Order, String, Float> pairUserIdValue() {
    return order -> new Tuple2<>(order.getUserId(), order.getAmount() * order.getPrice());
  }

}

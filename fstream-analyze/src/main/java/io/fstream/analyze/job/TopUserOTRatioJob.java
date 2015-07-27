/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.analyze.job;

import static io.fstream.analyze.util.Functions.computeLongRunningSum;
import static io.fstream.analyze.util.Functions.parseEvent;
import static io.fstream.analyze.util.SerializableComparator.serialize;
import static io.fstream.core.model.event.EventType.ORDER;
import static io.fstream.core.model.event.EventType.TRADE;
import static io.fstream.core.model.topic.Topic.METRICS;
import static io.fstream.core.model.topic.Topic.ORDERS;
import static io.fstream.core.model.topic.Topic.TRADES;
import io.fstream.analyze.core.Job;
import io.fstream.analyze.core.JobContext;
import io.fstream.analyze.kafka.KafkaProducer;
import io.fstream.core.model.event.MetricEvent;
import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Trade;
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Calculates a running total of all user / order values.
 */
@Slf4j
@Component
public class TopUserOTRatioJob extends Job {

  /**
   * The metric id.
   */
  private static final int ID = 13;

  /**
   * Top N.
   */
  private final int n;

  @Autowired
  public TopUserOTRatioJob(JobContext jobContext, @Value("${analyze.n}") int n) {
    super(topics(ORDERS, TRADES), jobContext);
    this.n = n;
  }

  @Override
  protected void plan(JavaPairReceiverInputDStream<String, String> kafkaStream) {
    analyzeStream(kafkaStream, topics, n, jobContext.getPool());
  }

  private static void analyzeStream(JavaPairReceiverInputDStream<String, String> kafkaStream,
      Set<Topic> topics, int n, Broadcast<ObjectPool<KafkaProducer>> pool) {
    log.info("[{}] Event count: {}", topics, kafkaStream.count());

    // Calculate order count by user
    val userOrderCounts =
        kafkaStream
            .map(parseEvent())
            .filter(event -> event.getType() == ORDER)
            .map(event -> (Order) event)
            .map(order -> order.getUserId())
            .countByValue()
            .updateStateByKey(computeLongRunningSum());

    // Get trade count by user
    val userTradeCounts =
        kafkaStream
            .map(parseEvent())
            .filter(event -> event.getType() == TRADE)
            .map(event -> (Trade) event)
            .flatMap(trade -> ImmutableList.of(trade.getBuyUser(), trade.getSellUser()))
            .countByValue()
            .updateStateByKey(computeLongRunningSum());

    // Join and calculate ratios
    val userOrderTradeRatios =
        userOrderCounts
            .join(userTradeCounts)
            .mapToPair((tuple) -> {
              String userId = tuple._1;
              float orderCount = tuple._2._1;
              float tradeCount = tuple._2._2;
              float ratio = orderCount / tradeCount;

              return new Tuple2<>(userId, ratio);
            });

    // Sort and top
    userOrderTradeRatios.foreachRDD((rdd, time) -> {
      log.info("[{}] Partition count: {}, user count: {}", topics, rdd.partitions().size(), rdd.count());
      analyzeBatch(rdd, time, n, pool);
      return null;
    });
  }

  @SneakyThrows
  private static void analyzeBatch(JavaPairRDD<String, Float> rdd, Time time, int n,
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

  private static Comparator<Tuple2<String, Float>> userValueDescending() {
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

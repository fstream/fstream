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
import static io.fstream.core.model.event.EventType.ORDER;
import static io.fstream.core.model.event.EventType.TRADE;
import static io.fstream.core.model.topic.Topic.ORDERS;
import static io.fstream.core.model.topic.Topic.TRADES;
import io.fstream.analyze.core.JobContext;
import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Trade;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import scala.Tuple2;

/**
 * Calculates a running total of all user / order values.
 */
@Slf4j
@Component
public class TopUserOTRatioJob extends TopUserJob {

  /**
   * The metric id.
   */
  private static final int ID = 13;

  @Autowired
  public TopUserOTRatioJob(JobContext jobContext, @Value("${analyze.n}") int n) {
    super(ID, topics(ORDERS, TRADES), n, jobContext);
  }

  @Override
  protected void plan(JavaPairReceiverInputDStream<String, String> kafkaStream) {
    log.info("[{}] Event count: {}", topics, kafkaStream.count());

    // Calculate order count by user
    val userOrderCounts =
        kafkaStream
            .map(parseEvent())
            .filter(event -> event.getType() == ORDER)
            .map(castEvent(Order.class))
            .map(order -> order.getUserId())
            .countByValue()
            .updateStateByKey(computeLongRunningSum());

    // Get trade count by user
    val userTradeCounts =
        kafkaStream
            .map(parseEvent())
            .filter(event -> event.getType() == TRADE)
            .map(castEvent(Trade.class))
            .flatMap(trade -> list(trade.getBuyUser(), trade.getSellUser()))
            .countByValue()
            .updateStateByKey(computeLongRunningSum());

    // Join and calculate ratios
    val userOrderTradeRatios =
        userOrderCounts
            .join(userTradeCounts)
            .mapToPair(calculateOrderTradeRatio());

    analyzeBatches(userOrderTradeRatios);
  }

  private static PairFunction<Tuple2<String, Tuple2<Long, Long>>, String, Float> calculateOrderTradeRatio() {
    return (tuple) -> {
      String userId = tuple._1;
      float orderCount = tuple._2._1;
      float tradeCount = tuple._2._2;
      float ratio = orderCount / tradeCount;

      return pair(userId, ratio);
    };
  }

}

/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.analyze.job;

import static io.fstream.analyze.util.EventFunctions.castEvent;
import static io.fstream.analyze.util.EventFunctions.filterEventType;
import static io.fstream.analyze.util.EventFunctions.parseEvent;
import static io.fstream.analyze.util.SumFunctions.runningSumLongs;
import static io.fstream.core.model.event.EventType.ORDER;
import static io.fstream.core.model.event.EventType.TRADE;
import static io.fstream.core.model.topic.Topic.ORDERS;
import static io.fstream.core.model.topic.Topic.TRADES;
import io.fstream.analyze.core.JobContext;
import io.fstream.core.model.definition.Metrics;
import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Trade;
import lombok.val;

import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import scala.Tuple2;

/**
 * Calculates "Top N Users by O/R Ratio" metric.
 */
@Component
public class TopUserOTRatiosJob extends TopUserJob<Float> {

  @Autowired
  public TopUserOTRatiosJob(JobContext jobContext, @Value("${analyze.n}") int n) {
    super(jobContext, Metrics.TOP_USER_OT_RATIO_ID, n, ORDERS, TRADES);
  }

  @Override
  protected JavaPairDStream<String, Float> planCalculation(JavaPairDStream<String, String> kafkaStream) {
    // Calculate order count by user
    val userOrderCounts =
        kafkaStream
            .map(parseEvent())
            .filter(filterEventType(ORDER))
            .map(castEvent(Order.class))
            .map(order -> order.getUserId())
            .countByValue()
            .updateStateByKey(runningSumLongs());

    // Get trade count by user
    val userTradeCounts =
        kafkaStream
            .map(parseEvent())
            .filter(filterEventType(TRADE))
            .map(castEvent(Trade.class))
            .flatMap(trade -> list(trade.getBuyUser(), trade.getSellUser()))
            .countByValue()
            .updateStateByKey(runningSumLongs());

    // Join and calculate ratios
    val userOrderTradeRatios =
        userOrderCounts
            .join(userTradeCounts)
            .mapToPair(calculateOrderTradeRatio());

    return userOrderTradeRatios;
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

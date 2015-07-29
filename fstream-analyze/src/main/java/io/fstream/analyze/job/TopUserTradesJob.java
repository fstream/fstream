/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.analyze.job;

import static io.fstream.analyze.util.EventFunctions.parseTrade;
import static io.fstream.analyze.util.SumFunctions.runningSumIntegers;
import static io.fstream.analyze.util.SumFunctions.sumIntegers;
import static io.fstream.core.model.topic.Topic.TRADES;
import io.fstream.analyze.core.JobContext;
import io.fstream.core.model.definition.Metrics;
import io.fstream.core.model.event.Trade;
import lombok.val;

import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Calculates "Top N Users by Trade" metric.
 */
@Component
public class TopUserTradesJob extends TopUserJob<Integer> {

  @Autowired
  public TopUserTradesJob(JobContext jobContext, @Value("${analyze.n}") int n) {
    super(jobContext, Metrics.TOP_USER_TRADES_ID, n, TRADES);
  }

  @Override
  protected JavaPairDStream<String, Integer> planCalculation(JavaPairDStream<String, String> kafkaStream) {
    // Calculate trade amounts by user
    val userTradeAmounts =
        kafkaStream
            .map(parseTrade())
            .flatMapToPair(pairUserTradeAmounts())
            .reduceByKey(sumIntegers())
            .updateStateByKey(runningSumIntegers());

    return userTradeAmounts;
  }

  private static PairFlatMapFunction<Trade, String, Integer> pairUserTradeAmounts() {
    return trade -> list(
        pair(trade.getBuyUser(), trade.getAmount()),
        pair(trade.getSellUser(), trade.getAmount()));
  }

}

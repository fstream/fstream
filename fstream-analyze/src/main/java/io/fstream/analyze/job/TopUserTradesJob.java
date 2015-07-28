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
import static io.fstream.analyze.util.Functions.parseTrade;
import static io.fstream.analyze.util.Functions.sumIntegerReducer;
import static io.fstream.core.model.topic.Topic.TRADES;
import io.fstream.analyze.core.JobContext;
import io.fstream.core.model.event.Trade;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Calculates a running total of all user / order values.
 */
@Slf4j
@Component
public class TopUserTradesJob extends TopUserJob {

  /**
   * The metric id.
   */
  private static final int ID = 11;

  @Autowired
  public TopUserTradesJob(JobContext jobContext, @Value("${analyze.n}") int n) {
    super(ID, topics(TRADES), n, jobContext);
  }

  @Override
  protected void plan(JavaPairReceiverInputDStream<String, String> kafkaStream) {
    log.info("[{}] Trade count: {}", topics, kafkaStream.count());

    // Get trade amounts by user
    val userTradeAmounts =
        kafkaStream
            .map(parseTrade())
            .flatMapToPair(tradeAmounts())
            .reduceByKey(sumIntegerReducer())
            .updateStateByKey(computeIntegerRunningSum());

    analyzeBatches(userTradeAmounts);
  }

  private static PairFlatMapFunction<Trade, String, Integer> tradeAmounts() {
    return trade -> list(
        pair(trade.getBuyUser(), trade.getAmount()),
        pair(trade.getSellUser(), trade.getAmount()));
  }

}

/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.analyze.job;

import static io.fstream.analyze.util.EventFunctions.parseOrder;
import static io.fstream.analyze.util.SumFunctions.runningSumIntegers;
import static io.fstream.analyze.util.SumFunctions.sumIntegers;
import static io.fstream.core.model.topic.Topic.ORDERS;
import io.fstream.analyze.core.JobContext;
import lombok.val;

import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Calculates "Top N Users by Order" metric.
 */
@Component
public class TopUserOrdersJob extends TopUserJob<Integer> {

  /**
   * The metric id.
   */
  private static final int TOP_USER_ORDERS_ID = 12;

  @Autowired
  public TopUserOrdersJob(JobContext jobContext, @Value("${analyze.n}") int n) {
    super(TOP_USER_ORDERS_ID, topics(ORDERS), n, jobContext);
  }

  @Override
  protected JavaPairDStream<String, Integer> planCalculation(JavaPairReceiverInputDStream<String, String> kafkaStream) {
    // Get order amounts by user
    val userOrderAmounts =
        kafkaStream
            .map(parseOrder())
            .mapToPair(order -> pair(order.getUserId(), order.getAmount()))
            .reduceByKey(sumIntegers())
            .updateStateByKey(runningSumIntegers());

    return userOrderAmounts;
  }

}

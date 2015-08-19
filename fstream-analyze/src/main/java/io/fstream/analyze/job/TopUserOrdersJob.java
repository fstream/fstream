/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.analyze.job;

import static io.fstream.analyze.util.EventFunctions.filterOrderType;
import static io.fstream.analyze.util.EventFunctions.parseOrder;
import static io.fstream.analyze.util.SumFunctions.runningSumIntegers;
import static io.fstream.analyze.util.SumFunctions.sumIntegers;
import static io.fstream.core.model.event.Order.OrderType.LIMIT_ADD;
import static io.fstream.core.model.topic.Topic.ORDERS;
import io.fstream.analyze.core.JobContext;
import io.fstream.core.model.definition.Metrics;
import lombok.val;

import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Calculates "Top N Users by Order" metric.
 */
@Component
public class TopUserOrdersJob extends TopUserJob<Integer> {

  @Autowired
  public TopUserOrdersJob(JobContext jobContext, @Value("${analyze.n}") int n) {
    super(jobContext, Metrics.TOP_USER_ORDERS_ID, n, ORDERS);
  }

  @Override
  protected JavaPairDStream<String, Integer> planCalculation(JavaPairDStream<String, String> kafkaStream) {
    // Get order amounts by user
    val userOrderAmounts =
        kafkaStream
            .map(parseOrder())
            .filter(filterOrderType(LIMIT_ADD))
            .mapToPair(order -> pair(order.getUserId(), order.getAmount()))
            .reduceByKey(sumIntegers())
            .updateStateByKey(runningSumIntegers());

    return userOrderAmounts;
  }

}

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
import static io.fstream.analyze.util.SumFunctions.runningSumFloats;
import static io.fstream.analyze.util.SumFunctions.sumFloats;
import static io.fstream.core.model.event.Order.OrderType.LIMIT_ADD;
import static io.fstream.core.model.topic.Topic.ORDERS;
import io.fstream.analyze.core.JobContext;
import io.fstream.core.model.definition.Metrics;
import io.fstream.core.model.event.Order;
import lombok.val;

import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Calculates "Top N Users by Value" metric.
 */
@Component
public class TopUserValuesJob extends TopUserJob<Float> {

  @Autowired
  public TopUserValuesJob(JobContext jobContext, @Value("${analyze.n}") int n) {
    super(jobContext, Metrics.TOP_USER_VALUE_ID, n, ORDERS);
  }

  @Override
  protected JavaPairDStream<String, Float> planCalculation(JavaPairDStream<String, String> kafkaStream) {
    // Calculate value by user
    val userValues =
        kafkaStream
            .map(parseOrder())
            .filter(filterOrderType(LIMIT_ADD))
            .mapToPair(order -> pair(order.getUserId(), calculateOrderValue(order)))
            .reduceByKey(sumFloats())
            .updateStateByKey(runningSumFloats());

    return userValues;
  }

  /**
   * Main business method that defines the "metric" per user id.
   */
  private static float calculateOrderValue(Order order) {
    return order.getAmount() * order.getPrice();
  }

}

/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.analyze.job;

import static io.fstream.analyze.util.Functions.computeFloatRunningSum;
import static io.fstream.analyze.util.Functions.parseOrder;
import static io.fstream.analyze.util.Functions.sumFloatReducer;
import static io.fstream.core.model.topic.Topic.ORDERS;
import io.fstream.analyze.core.JobContext;
import io.fstream.core.model.event.Order;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Calculates a running total of all user / order values.
 */
@Slf4j
@Component
public class TopUserValuesJob extends TopUserJob {

  /**
   * The metric id.
   */
  private static final int ID = 10;

  @Autowired
  public TopUserValuesJob(JobContext jobContext, @Value("${analyze.n}") int n) {
    super(ID, topics(ORDERS), n, jobContext);
  }

  @Override
  protected void plan(JavaPairReceiverInputDStream<String, String> kafkaStream) {
    log.info("[{}] Order count: {}", topics, kafkaStream.count());

    // Define
    val userValues =
        kafkaStream
            .map(parseOrder())
            .mapToPair(order -> pair(order.getUserId(), calculateOrderValue(order)))
            .reduceByKey(sumFloatReducer())
            .updateStateByKey(computeFloatRunningSum());

    analyzeBatches(userValues);
  }

  /**
   * Main business method that defines the "metric" per user id.
   */
  private static float calculateOrderValue(Order order) {
    return order.getAmount() * order.getPrice();
  }

}

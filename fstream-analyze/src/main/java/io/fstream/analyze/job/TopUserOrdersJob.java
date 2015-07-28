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
import static io.fstream.analyze.util.Functions.parseOrder;
import static io.fstream.analyze.util.Functions.sumIntegerReducer;
import static io.fstream.core.model.topic.Topic.ORDERS;
import io.fstream.analyze.core.JobContext;
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
public class TopUserOrdersJob extends TopUserJob {

  /**
   * The metric id.
   */
  private static final int ID = 12;

  @Autowired
  public TopUserOrdersJob(JobContext jobContext, @Value("${analyze.n}") int n) {
    super(ID, topics(ORDERS), n, jobContext);
  }

  @Override
  protected void plan(JavaPairReceiverInputDStream<String, String> kafkaStream) {
    log.info("[{}] Order count: {}", topics, kafkaStream.count());

    // Get order amounts by user
    val userOrderAmounts =
        kafkaStream
            .map(parseOrder())
            .mapToPair(order -> pair(order.getUserId(), order.getAmount()))
            .reduceByKey(sumIntegerReducer())
            .updateStateByKey(computeIntegerRunningSum());

    analyzeBatches(userOrderAmounts);
  }

}

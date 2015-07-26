/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.analyze.job;

import static io.fstream.analyze.util.Functions.computeRunningSum;
import static io.fstream.analyze.util.Functions.mapUserIdAmount;
import static io.fstream.analyze.util.Functions.parseOrder;
import static io.fstream.analyze.util.Functions.sumReducer;
import io.fstream.analyze.kafka.KafkaProducer;
import io.fstream.core.model.topic.Topic;

import java.util.Iterator;
import java.util.Set;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.pool2.ObjectPool;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.springframework.stereotype.Service;

import scala.Tuple2;

import com.google.common.collect.ImmutableSet;

/**
 * Service responsible for persisting to the long-term HDFS backing store.
 * <p>
 * This class is <em>not</em> thread-safe.
 */
@Slf4j
@Service
public class TopUserValueAnalysisJob extends AnalysisJob {

  public TopUserValueAnalysisJob() {
    super(ImmutableSet.of(Topic.ORDERS));
  }

  @Override
  protected void analyze(SQLContext sqlContext, JavaPairReceiverInputDStream<String, String> kafkaStream,
      Broadcast<ObjectPool<KafkaProducer>> pool) {
    analyzeStream(sqlContext, kafkaStream, pool, topics);
  }

  private static void analyzeStream(SQLContext sqlContext, JavaPairReceiverInputDStream<String, String> kafkaStream,
      Broadcast<ObjectPool<KafkaProducer>> pool, Set<Topic> topics) {

    log.info("[{}] Order count: {}", topics, kafkaStream.count());

    // Define
    val aggregatedUserAmounts = kafkaStream
        .map(parseOrder())
        .mapToPair(mapUserIdAmount())
        .reduceByKey(sumReducer())
        .updateStateByKey(computeRunningSum());

    aggregatedUserAmounts.foreachRDD((rdd, time) -> {
      log.info("[{}] Partition count: {}, order count: {}", topics, rdd.partitions().size(), rdd.count());
      analyzeBatch(rdd, time, pool, sqlContext);
      return null;
    });
  }

  private static void analyzeBatch(JavaPairRDD<String, Long> rdd, Time time,
      Broadcast<ObjectPool<KafkaProducer>> pool, SQLContext sqlContext) {

    rdd.foreachPartition((partition) -> {
      KafkaProducer producer = pool.getValue().borrowObject();
      analyzeBatchPartition(time, partition, producer);

      pool.getValue().returnObject(producer);
    });
  }

  private static void analyzeBatchPartition(Time time, Iterator<Tuple2<String, Long>> partition, KafkaProducer producer) {
    partition.forEachRemaining(userAmount -> {
      log.info("User Amount: {} = {}", userAmount._1, userAmount._2);
      producer.send(userAmount);
    });
  }

}

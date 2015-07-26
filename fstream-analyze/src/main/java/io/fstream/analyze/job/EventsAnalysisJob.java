/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.analyze.job;

import static io.fstream.analyze.util.Functions.parseEvents;
import io.fstream.analyze.kafka.KafkaProducer;
import io.fstream.core.model.event.Event;
import io.fstream.core.model.topic.Topic;

import java.util.Iterator;
import java.util.Set;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.pool2.ObjectPool;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;

import com.google.common.collect.ImmutableSet;

/**
 * Echos all incoming Kafka events to the "result" topic in Kafka.
 * <p>
 * For demo purposes only!
 */
@Slf4j
// @Service
// Note: Right now each job must run in a separate JVM.
public class EventsAnalysisJob extends AnalysisJob {

  public EventsAnalysisJob() {
    super(ImmutableSet.of(Topic.ORDERS, Topic.TRADES, Topic.QUOTES));
  }

  @Override
  protected void analyze(SQLContext sqlContext, JavaPairReceiverInputDStream<String, String> kafkaStream,
      Broadcast<ObjectPool<KafkaProducer>> pool) {
    analyzeStream(sqlContext, kafkaStream, pool, topics);
  }

  private static void analyzeStream(SQLContext sqlContext, JavaPairReceiverInputDStream<String, String> kafkaStream,
      Broadcast<ObjectPool<KafkaProducer>> pool, Set<Topic> topics) {
    log.info("[{}] message count: {}", topics, kafkaStream.count());

    // Define
    val events = kafkaStream
        .map(parseEvents());

    events.foreachRDD((rdd, time) -> {
      log.info("[{}] Partition count: {}, event count: {}", topics, rdd.partitions().size(), rdd.count());
      analyzeBatch(rdd, time, pool, sqlContext);
      return null;
    });
  }

  private static void analyzeBatch(JavaRDD<Event> rdd, Time time,
      Broadcast<ObjectPool<KafkaProducer>> pool, SQLContext sqlContext) {

    rdd.foreachPartition((partition) -> {
      KafkaProducer producer = pool.getValue().borrowObject();
      analyzeBatchPartition(time, partition, producer);

      pool.getValue().returnObject(producer);
    });
  }

  private static void analyzeBatchPartition(Time time, Iterator<Event> partition, KafkaProducer producer) {
    partition.forEachRemaining(event -> {
      log.info("Event = {}", event);
      producer.send(event);
    });
  }

}

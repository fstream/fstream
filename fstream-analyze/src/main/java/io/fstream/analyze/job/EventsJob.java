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
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;

import com.google.common.collect.ImmutableSet;

/**
 * Echos all incoming Kafka events to the "result" topic in Kafka.
 * <p>
 * For demo purposes only!
 */
@Slf4j
public class EventsJob extends Job {

  public EventsJob(JobContext context) {
    super(ImmutableSet.of(Topic.ORDERS, Topic.TRADES, Topic.QUOTES), context);
  }

  @Override
  protected void analyze(JavaPairReceiverInputDStream<String, String> kafkaStream) {
    analyzeStream(kafkaStream, jobContext.getPool(), topics);
  }

  private static void analyzeStream(JavaPairReceiverInputDStream<String, String> kafkaStream,
      Broadcast<ObjectPool<KafkaProducer>> pool, Set<Topic> topics) {
    log.info("[{}] message count: {}", topics, kafkaStream.count());

    // Define
    val events = kafkaStream
        .map(parseEvents());

    events.foreachRDD((rdd, time) -> {
      log.info("[{}] Partition count: {}, event count: {}", topics, rdd.partitions().size(), rdd.count());
      analyzeBatch(rdd, time, pool);
      return null;
    });
  }

  private static void analyzeBatch(JavaRDD<Event> rdd, Time time, Broadcast<ObjectPool<KafkaProducer>> pool) {
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

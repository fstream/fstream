/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.analyze.job;

import io.fstream.analyze.kafka.KafkaProducer;
import io.fstream.core.model.topic.Topic;

import java.util.Iterator;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.pool2.ObjectPool;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.streaming.Time;

import scala.Tuple2;

/**
 * Service responsible for persisting to the long-term HDFS backing store.
 * <p>
 * This class is <em>not</em> thread-safe.
 */
@Slf4j
public class DefaultAnalysisJob extends AnalysisJob {

  @Override
  protected void analyzeBatch(JavaPairRDD<String, String> rdd, Time time, Topic topic,
      Broadcast<ObjectPool<KafkaProducer>> pool, SQLContext sqlContext) {
    // Remove key
    val messages = rdd.map(Tuple2::_2);
    log.info("[{}] Partition count: {}, message count: {}", topic, messages.partitions().size(), messages.count());

    messages.foreachPartition((partition) -> {
      KafkaProducer producer = pool.getValue().borrowObject();
      analyzeBatchPartition(time, partition, producer);

      pool.getValue().returnObject(producer);
    });
  }

  private void analyzeBatchPartition(Time time, Iterator<String> partition, KafkaProducer producer) {
    partition.forEachRemaining(message -> {
      producer.send(time);
    });
  }

}

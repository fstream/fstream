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
import io.fstream.core.config.KafkaProperties;
import lombok.Value;

import org.apache.commons.pool2.ObjectPool;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.SQLContext;

@Value
public class JobContext {

  KafkaProperties kafkaProperties;
  SQLContext sqlContext;
  Broadcast<ObjectPool<KafkaProducer>> pool;

}
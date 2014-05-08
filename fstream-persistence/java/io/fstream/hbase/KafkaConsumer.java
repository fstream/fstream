/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.hbase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

/**
 * 
 */
public class KafkaConsumer {

  private final ConsumerConnector consumer;
  private final String topic = "test";

  public KafkaConsumer()
  {
    consumer = kafka.consumer.Consumer.createJavaConsumerConnector(createConsumerConfig());
  }

  private static ConsumerConfig createConsumerConfig()
  {
    Properties props = new Properties() {

      {
        put("zookeeper.connect", "localhost:21818");
        put("zookeeper.connection.timeout.ms", "1000000");
        put("group.id", "1");
        put("broker.id", "0");
      }

    };

    return new ConsumerConfig(props);

  }

  public void run() {
    Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
    topicCountMap.put(topic, new Integer(1));
    Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
    KafkaStream<byte[], byte[]> stream = consumerMap.get(topic).get(0);
    ConsumerIterator it = stream.iterator();
    while (it.hasNext())
      System.out.println(it.next().toString());
  }

}

/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.hbase;

import io.fstream.core.model.Rate;
import io.fstream.schema.FstreamHbaseSchema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.Message;
import kafka.message.MessageAndMetadata;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 */
@Slf4j
public class KafkaConsumer extends Thread {

  private final ConsumerConnector consumer;
  private final String topic = "test";
  private static final ObjectMapper MAPPER = new ObjectMapper();

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

  @Override
  @SneakyThrows
  public void run() {
    Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
    topicCountMap.put(topic, new Integer(1));
    Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
    KafkaStream<byte[], byte[]> stream = consumerMap.get(topic).get(0);
    ConsumerIterator it = stream.iterator();
    // while (it.hasNext())
    // System.out.println(new String(it.next().message()));
    FstreamHbaseSchema hbaseclient = new FstreamHbaseSchema();
    for (val messageAndMetadata : stream) {
      val message = messageAndMetadata.message();
      val text = new String(message);
      val rate = MAPPER.readValue(text, Rate.class);
      log.info("hbase consumer received: {}", rate.getDateTime());
      hbaseclient.addRow(rate);
      //template.send("/topic/rates", convert(message));
    }
  }

  public static void main(String[] args) {
    (new KafkaConsumer()).start();
  }

}

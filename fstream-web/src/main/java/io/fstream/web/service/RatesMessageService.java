/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.web.service;

import static com.google.common.collect.ImmutableMap.of;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

@Slf4j
@Service
public class RatesMessageService extends AbstractExecutionThreadService {

  @Autowired
  private SimpMessagingTemplate template;

  private KafkaStream<byte[], byte[]> stream;
  private ConsumerConnector consumerConnector;

  @PostConstruct
  public void init() throws Exception {
    log.info("Initializing...");
    val props = new Properties();
    props.put("zookeeper.connect", "localhost:21818");
    props.put("zookeeper.connection.timeout.ms", "1000000");
    props.put("group.id", "1");
    props.put("broker.id", "0");

    this.consumerConnector = Consumer.createJavaConsumerConnector(new ConsumerConfig(props));

    val count = 1;
    val topicMessageStreams = consumerConnector.createMessageStreams(of("test", count));
    val streams = topicMessageStreams.get("test");

    this.stream = streams.get(0);

    startAsync();
  }

  @PreDestroy
  public void destroy() throws Exception {
    log.info("Destroying...");
    stopAsync();

    consumerConnector.commitOffsets();
    consumerConnector.shutdown();
  }

  @Override
  protected void run() throws Exception {
    log.info("Running!");

    for (val messageAndMetadata : stream) {
      val message = messageAndMetadata.message();
      val text = new String(message);

      log.info("Received: {}", text);
      template.send("/topic/greetings", MessageBuilder.withPayload(message).build());
    }
  }

}

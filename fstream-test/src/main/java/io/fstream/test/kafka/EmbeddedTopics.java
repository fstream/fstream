/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.test.kafka;

import static com.google.common.base.Strings.repeat;
import static com.google.common.collect.ImmutableList.of;
import io.fstream.core.model.topic.Topic;
import kafka.javaapi.TopicMetadataRequest;
import kafka.javaapi.consumer.SimpleConsumer;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class EmbeddedTopics {

  /**
   * Configuration.
   */
  @Value("${kafka.host}")
  private String kafkaHost;
  @Value("${kafka.port}")
  private int kafkaPort;

  public void create() {
    val consumer = new SimpleConsumer(kafkaHost, kafkaPort, 60 * 1000, 64 * 1024, "fstream-test");
    try {
      for (val topic : Topic.values()) {
        val topicName = topic.getId();
        log.info(repeat("-", 80));
        log.info("Creating topic '{}'...", topicName);
        log.info(repeat("-", 80));

        // Create topics by requesting meta data
        val request = new TopicMetadataRequest(of(topicName));
        consumer.send(request);
      }
    } finally {
      consumer.close();
    }
  }

}

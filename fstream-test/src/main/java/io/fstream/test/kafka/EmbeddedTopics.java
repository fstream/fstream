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
import io.fstream.core.model.event.AlertEvent;
import io.fstream.core.model.event.MetricEvent;
import io.fstream.core.model.event.TickEvent;
import io.fstream.core.util.Codec;

import java.util.Properties;

import kafka.admin.AdminUtils;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import kafka.utils.ZKStringSerializer;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;

@Slf4j
@RequiredArgsConstructor
public class EmbeddedTopics {

  /**
   * Configuration.
   */
  private final String zkConnect;

  public void create() {
    create("rates", new TickEvent());
    create("alerts", new AlertEvent());
    create("metrics", new MetricEvent());
    list();
  }

  private void create(String topicName, Object data) {
    val producerProperties = new Properties();
    producerProperties.put("metadata.broker.list", "localhost:6667");
    producerProperties.put("serializer.class", "kafka.serializer.StringEncoder");
    producerProperties.put("request.required.acks", "1");

    val config = new ProducerConfig(producerProperties);
    val producer = new Producer<String, String>(config);
    val message = new KeyedMessage<String, String>(topicName, Codec.encodeText(data));

    log.info("***** Creating topic '{}'...", topicName);
    producer.send(message);
    producer.close();
    log.info("***** Finished creating topic '{}'", topicName);
  }

  private void list() {
    val zkClient = new ZkClient(zkConnect);
    zkClient.setZkSerializer(new ZkSerializer() {

      @Override
      public byte[] serialize(Object o) throws ZkMarshallingError {
        return ZKStringSerializer.serialize(o);
      }

      @Override
      public Object deserialize(byte[] bytes) throws ZkMarshallingError {
        return ZKStringSerializer.deserialize(bytes);
      }

    });

    val topicConfigs = AdminUtils.fetchAllTopicConfigs(zkClient);
    val iterator = topicConfigs.keySet().iterator();
    while (iterator.hasNext()) {
      val topicName = iterator.next();
      log.info(repeat("-", 80));
      log.info("Topic '{}'", topicName);
      log.info(repeat("-", 80));

      val topicConfig = topicConfigs.get(topicName);
      val topicMetadata = AdminUtils.fetchTopicMetadataFromZk(topicName, zkClient);

      log.info("  - Config:   {}", topicConfig.get());
      log.info("  - Metadata: {}", topicMetadata.partitionsMetadata());
      log.info(repeat("-", 80));
      log.info("");
    }
  }

}

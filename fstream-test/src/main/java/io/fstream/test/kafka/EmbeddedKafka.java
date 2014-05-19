/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.test.kafka;

import java.util.Properties;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import lombok.val;

import com.google.common.util.concurrent.AbstractIdleService;

public final class EmbeddedKafka extends AbstractIdleService {

  private final KafkaServerStartable server;

  public EmbeddedKafka() {
    val properties = new Properties();
    properties.put("zookeeper.connect", "localhost:21812");
    properties.put("broker.id", "0");

    server = new KafkaServerStartable(new KafkaConfig(properties));
  }

  @Override
  protected void startUp() throws Exception {
    server.startup();
  }

  @Override
  protected void shutDown() throws Exception {
    server.shutdown();
    server.awaitShutdown();
  }

}
/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.test.kafka;

import java.io.File;
import java.util.Properties;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class EmbeddedKafka {

  /**
   * Configuration.
   */
  @NonNull
  private final String zkConnect;
  @NonNull
  private final File logDir;

  /**
   * State.
   */
  private KafkaServerStartable server;

  public void startUp() throws Exception {
    val properties = createProperties();
    server = new KafkaServerStartable(new KafkaConfig(properties));

    log.info("Starting up server using logDir '{}'...", logDir.getAbsolutePath());
    server.startup();
    log.info("Finished startup");
  }

  public void shutDown() throws Exception {
    log.info("Shutting down server...");
    server.shutdown();
    log.info("Awaiting shutdown...");
    server.awaitShutdown();
    log.info("Finished shutdown");
  }

  /**
   * @see https://kafka.apache.org/08/configuration.html
   */
  private Properties createProperties() {
    log.info("Creating properties with zkConnect = '{}'", zkConnect);
    val properties = new Properties();
    properties.put("zookeeper.connect", zkConnect);
    properties.put("port", "6667");
    properties.put("broker.id", "0");

    // Max size of each topic partition log:
    properties.put("log.retention.bytes", "2147483648"); // 2 GB

    properties.put("log.dirs", logDir.getAbsolutePath());

    return properties;
  }

}
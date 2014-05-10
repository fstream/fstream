/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.test;

import static java.lang.System.in;
import static java.lang.System.out;
import io.fstream.test.kafka.EmbeddedKafka;
import io.fstream.test.kafka.EmbeddedZooKeeper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import java.util.concurrent.Executors;

import kafka.admin.AdminUtils;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.I0Itec.zkclient.ZkClient;

import com.google.common.collect.ImmutableMap;

@Slf4j
public class Main {

  File tmp;
  EmbeddedZooKeeper zooKeeper;
  EmbeddedKafka kafka;

  @SneakyThrows
  public static void main(String[] args) {
    val main = new Main();
    try {
      main.tmp = Files.createTempDirectory("fstream-test").toFile();
      main.setUp();
      main.testServer();
    } finally {
      main.tearDown();
      main.tmp.delete();
    }
  }

  public void setUp() throws IOException {
    log.info("Testing storage: {}", tmp);
    
    log.info("> Starting embedded ZooKeeper...");
    zooKeeper = new EmbeddedZooKeeper(tmp, tmp);
    zooKeeper.startAsync();
    zooKeeper.awaitRunning();
    log.info("< Started embedded ZooKeeper");

    log.info("> Starting embedded Kafka...");
    kafka = new EmbeddedKafka();
    kafka.startAsync();
    kafka.awaitRunning();
    log.info("< Started embedded Kafka");
  }

  public void tearDown() throws IOException {
    log.info("> Stopping embedded Kafka...");
    kafka.stopAsync();
    kafka.awaitTerminated();
    log.info("< Stopped embedded Kafka");

    log.info("Stopping embedded ZooKeeper...");
    zooKeeper.stopAsync();
    zooKeeper.awaitTerminated();
    log.info("Stopped embedded ZooKeeper");
  }

  public void testServer() throws IOException {
    // createTopic();

    // registerConsumer();
    out.println("\n\n*** Running embedded ZooKeeper / Kafka. Press enter to shutdown\n\n");
    in.read();
  }

  @SuppressWarnings("unused")
  private void registerConsumer() {
    val props = new Properties();
    props.put("zookeeper.connect", "localhost:21818");
    props.put("zookeeper.connection.timeout.ms", "1000000");
    props.put("group.id", "1");
    props.put("broker.id", "0");

    val consumerConnector = Consumer.createJavaConsumerConnector(new ConsumerConfig(props));

    val count = 1;
    val definition = ImmutableMap.of("rates", count);
    val topicMessageStreams = consumerConnector.createMessageStreams(definition);
    val streams = topicMessageStreams.get("rates");
    val executor = Executors.newFixedThreadPool(count);

    for (val stream : streams) {
      executor.submit(new Runnable() {

        @Override
        public void run() {
          for (val event : stream) {
            log.info("Received message: {}", new String(event.message()));
          }
        }

      });
    }
  }

  @SuppressWarnings("unused")
  private void createTopic() {
    val zkClient = new ZkClient("localhost:21818");
    Properties props = new Properties();
    String topic = "rates";
    int partitions = 1;
    int replicationFactor = 1;
    AdminUtils.createTopic(zkClient, topic, partitions, replicationFactor, props);
    zkClient.close();
  }

}

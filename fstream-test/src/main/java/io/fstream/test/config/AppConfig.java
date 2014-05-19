/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.test.config;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.val;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import io.fstream.test.hbase.EmbeddedHBase;
import io.fstream.test.kafka.EmbeddedKafka;

import java.io.File;
import java.nio.file.Files;
import java.util.Properties;
import java.util.concurrent.Executors;

import kafka.admin.AdminUtils;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.I0Itec.zkclient.ZkClient;
import org.springframework.context.annotation.Bean;

import com.google.common.collect.ImmutableMap;

@Slf4j
@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties
public class AppConfig {

  @Bean
  @SneakyThrows
  public File tmp() {
   val tmp = Files.createTempDirectory("fstream-test").toFile();
   log.info("Testing storage: {}", tmp);
   
   return tmp;
  }
  
  @Bean
  public EmbeddedHBase embeddedHbase() {
    return new EmbeddedHBase();
  }
  
//  @Bean
//  @SneakyThrows
//  public EmbeddedZooKeeper embeddedZooKeeper() {
//    return new EmbeddedZooKeeper(tmp(), tmp());
//  }
//  
  @Bean
  @SneakyThrows
  public EmbeddedKafka embeddedKafka() {
    return new EmbeddedKafka();
  }
  
  @PostConstruct
  public void init() {
    log.info("> Starting embedded ZooKeeper...");
    embeddedHbase().startAndWait();
    log.info("< Started embedded ZooKeeper");

    log.info("> Starting embedded Kafka...");
    embeddedKafka().startAndWait();
    log.info("< Started embedded Kafka");
  }
  
  @PreDestroy
  public void destroy() {
    log.info("> Stopping embedded Kafka...");
    embeddedKafka().stopAndWait();
    log.info("< Stopped embedded Kafka");

    log.info("Stopping embedded ZooKeeper...");
    embeddedHbase().stopAndWait();
    log.info("Stopped embedded ZooKeeper");
  }

  @SuppressWarnings("unused")
  private void registerConsumer() {
    val props = new Properties();
    props.put("zookeeper.connect", "localhost:21812");
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
    val zkClient = new ZkClient("localhost:21812");
    Properties props = new Properties();
    String topic = "rates";
    int partitions = 1;
    int replicationFactor = 1;
    AdminUtils.createTopic(zkClient, topic, partitions, replicationFactor, props);
    zkClient.close();
  }
  
}

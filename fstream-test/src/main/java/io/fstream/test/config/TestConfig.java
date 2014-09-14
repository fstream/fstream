/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.test.config;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.repeat;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import io.fstream.core.config.CoreConfig;
import io.fstream.core.model.topic.Topic;
import io.fstream.test.hbase.EmbeddedHBase;
import io.fstream.test.kafka.EmbeddedKafka;
import io.fstream.test.kafka.KafkaUtils;
import io.fstream.test.kafka.ZkStringSerializer;
import io.fstream.test.zk.EmbeddedZooKeeper;

import java.io.File;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Slf4j
@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties
public class TestConfig extends CoreConfig {

  /**
   * Configuration.
   */
  @Value("${zk.connect}")
  private String zkConnect;

  private static File WORK_DIR = new File("/tmp/fstream-test");

  @Bean
  @SneakyThrows
  public File workDir() {
    val workDir = WORK_DIR;
    if (workDir.exists()) {
      deleteDirectory(workDir);
    }

    checkState(workDir.mkdir(), "Could not create %s", workDir);
    log.info("**** Testing storage: {}", workDir);

    return workDir;
  }

  @Bean
  public boolean hbase() {
    return false;
  }

  @Bean
  @ConditionalOnExpression("false")
  public EmbeddedHBase embeddedHbase() {
    return new EmbeddedHBase(zkConnect);
  }

  @Bean
  public EmbeddedZooKeeper embeddedZookeeper() {
    return new EmbeddedZooKeeper(zkConnect, new File(workDir(), "zookeeper"));
  }

  @Bean
  @SneakyThrows
  public EmbeddedKafka embeddedKafka() {
    return new EmbeddedKafka(zkConnect, new File(workDir(), "kafka"));
  }

  @Bean
  @Lazy
  @SneakyThrows
  public ZkClient zkClient() {
    val zkClient = new ZkClient(zkConnect);
    zkClient.setZkSerializer(new ZkStringSerializer());

    return zkClient;
  }

  @PostConstruct
  @SneakyThrows
  public void init() {
    if (hbase()) {
      log.info("> Starting embedded HBase...\n");
      embeddedHbase().startUp();
      log.info("< Started embedded HBase");
    } else {
      log.info("> Starting embedded ZooKeeper...\n");
      embeddedZookeeper().startUp();
      log.info("< Started embedded ZooKeeper");
    }

    log.info("> Starting embedded Kafka...\n");
    embeddedKafka().startUp();
    log.info("< Started embedded Kafka");

    log.info("> Creating topics...");
    for (val topic : Topic.values()) {
      val topicName = topic.getId();
      log.info(repeat("-", 80));
      log.info("Creating topic '{}'...", topicName);
      log.info(repeat("-", 80));

      KafkaUtils.createTopic(zkClient(), topicName);
    }
    log.info("< Created topics");

    log.info("> Initializing state...");
    stateService.initialize();
    stateService.setState(state);
    log.info("< Initialized state");
  }

  @PreDestroy
  @SneakyThrows
  public void destroy() {
    log.info("> Stopping embedded Kafka...");
    embeddedKafka().shutDown();
    log.info("< Stopped embedded Kafka");

    if (hbase()) {
      log.info("> Stopping embedded HBase...");
      embeddedHbase().shutDown();
      log.info("< Stopped embedded HBase");
    } else {
      log.info("> Stopping embedded ZooKeeper...");
      embeddedZookeeper().shutDown();
      log.info("< Stopped embedded ZooKeeper");
    }

    log.info("> Stopping ZkClient...");
    zkClient().close();
    log.info("< Stopped ZkClient");
  }

}

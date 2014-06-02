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
import static org.apache.commons.io.FileUtils.deleteDirectory;
import io.fstream.test.hbase.EmbeddedHBase;
import io.fstream.test.kafka.EmbeddedKafka;
import io.fstream.test.kafka.EmbeddedTopics;
import io.fstream.test.zk.EmbeddedZooKeeper;

import java.io.File;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties
public class AppConfig {

  @Value("${zk.connect}")
  private String zkConnect;

  @Bean
  @SneakyThrows
  public File workDir() {
    val workDir = new File("/tmp/fstream-test");
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
    return new EmbeddedZooKeeper(zkConnect, workDir());
  }

  @Bean
  @SneakyThrows
  public EmbeddedKafka embeddedKafka() {
    return new EmbeddedKafka(zkConnect, workDir());
  }

  @Bean
  @SneakyThrows
  public EmbeddedTopics embeddedTopics() {
    return new EmbeddedTopics();
  }

  @PostConstruct
  @SneakyThrows
  public void init() {
    if (hbase()) {
      log.info("> Starting embedded HBase...");
      embeddedHbase().startUp();
      log.info("< Started embedded HBase");
    } else {
      log.info("> Starting embedded ZooKeeper...");
      embeddedZookeeper().startUp();
      log.info("< Started embedded ZooKeeper");
    }

    log.info("> Starting embedded Kafka...");
    embeddedKafka().startUp();
    log.info("< Started embedded Kafka");

    log.info("> Creating embedded topics...");
    embeddedTopics().create();
    log.info("< Created embedded topics");
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
  }

}

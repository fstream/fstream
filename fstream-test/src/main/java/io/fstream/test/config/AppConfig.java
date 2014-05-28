/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.test.config;

import io.fstream.test.hbase.EmbeddedHBase;
import io.fstream.test.kafka.EmbeddedKafka;
import io.fstream.test.zk.EmbeddedZooKeeper;

import java.io.File;
import java.nio.file.Files;

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
  public File tmp() {
    val tmp = Files.createTempDirectory("fstream-test").toFile();
    log.info("Testing storage: {}", tmp);

    return tmp;
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
    return new EmbeddedZooKeeper(zkConnect, tmp());
  }

  @Bean
  @SneakyThrows
  public EmbeddedKafka embeddedKafka() {
    return new EmbeddedKafka(zkConnect);
  }

  @PostConstruct
  public void init() {
    if (hbase()) {
      log.info("> Starting embedded HBase...");
      embeddedHbase().startAndWait();
      log.info("< Started embedded HBase");
    } else {
      log.info("> Starting embedded ZooKeeper...");
      embeddedZookeeper().startAndWait();
      log.info("< Started embedded ZooKeeper");
    }

    log.info("> Starting embedded Kafka...");
    embeddedKafka().startAndWait();
    log.info("< Started embedded Kafka");
  }

  @PreDestroy
  public void destroy() {
    log.info("> Stopping embedded Kafka...");
    embeddedKafka().stopAndWait();
    log.info("< Stopped embedded Kafka");

    if (hbase()) {
      log.info("> Stopping embedded HBase...");
      embeddedHbase().stopAndWait();
      log.info("< Stopped embedded HBase");
    } else {
      log.info("> Stopping embedded ZooKeeper...");
      embeddedZookeeper().stopAndWait();
      log.info("< Stopped embedded ZooKeeper");
    }
  }

}

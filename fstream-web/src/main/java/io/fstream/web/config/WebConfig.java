/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.web.config;

import static io.fstream.core.model.topic.Topic.ALERTS;
import static io.fstream.core.model.topic.Topic.METRICS;
import static io.fstream.core.model.topic.Topic.ORDERS;
import static io.fstream.core.model.topic.Topic.QUOTES;
import static io.fstream.core.model.topic.Topic.TRADES;
import io.fstream.core.config.CoreConfig;
import io.fstream.web.service.TopicMessageService;

import javax.annotation.PostConstruct;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Java config for Spring consumption.
 */
@Slf4j
@Configuration
public class WebConfig extends CoreConfig {

  @Bean
  public TopicMessageService tradesMessageService() {
    return new TopicMessageService(TRADES);
  }

  @Bean
  public TopicMessageService ordersMessageService() {
    return new TopicMessageService(ORDERS);
  }

  @Bean
  public TopicMessageService quotesMessageService() {
    return new TopicMessageService(QUOTES);
  }

  @Bean
  public TopicMessageService alertsMessageService() {
    return new TopicMessageService(ALERTS);
  }

  @Bean
  public TopicMessageService metricsMessageService() {
    return new TopicMessageService(METRICS);
  }

  @PostConstruct
  @SneakyThrows
  public void init() {
    log.info("> Initializing state...");
    stateService.initialize();
    stateService.setState(state);
    log.info("< Initialized state");
  }

}

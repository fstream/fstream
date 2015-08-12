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
import io.fstream.core.service.LocalStateService;
import io.fstream.core.service.StateService;
import io.fstream.web.service.TopicMessageService;

import javax.annotation.PostConstruct;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Java config for Spring consumption.
 */
@Slf4j
@Configuration
public class WebConfig extends CoreConfig {

  @Bean
  @Profile("kafka")
  public TopicMessageService tradesMessageService() {
    log.info("Starting trade stream...");
    return new TopicMessageService(TRADES);
  }

  @Bean
  @Profile("kafka")
  public TopicMessageService ordersMessageService() {
    log.info("Starting order stream...");
    return new TopicMessageService(ORDERS);
  }

  @Bean
  @Profile("kafka")
  public TopicMessageService quotesMessageService() {
    log.info("Starting quote stream...");
    return new TopicMessageService(QUOTES);
  }

  @Bean
  @Profile("kafka")
  public TopicMessageService alertsMessageService() {
    return new TopicMessageService(ALERTS);
  }

  @Bean
  @Profile("kafka")
  public TopicMessageService metricsMessageService() {
    log.info("Starting metrics stream...");
    return new TopicMessageService(METRICS);
  }

  @Bean
  @Profile("!kafka")
  public StateService stateService() {
    log.info("Starting local state service...");
    return new LocalStateService(state);
  }

  @PostConstruct
  @SneakyThrows
  public void init() {
    if (isProfilesActive("kafka")) {
      log.info("> Initializing state...");
      stateService.initialize();
      stateService.setState(state);
      log.info("< Initialized state");
    }
  }

}

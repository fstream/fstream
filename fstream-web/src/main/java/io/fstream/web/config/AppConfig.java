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
import static io.fstream.core.model.topic.Topic.RATES;
import io.fstream.web.service.TopicMessageService;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Java config for Spring consumption.
 */
@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties
public class AppConfig {

  @Bean
  public TopicMessageService ratesMessageService() {
    return new TopicMessageService(RATES);
  }

  @Bean
  public TopicMessageService alertMessageService() {
    return new TopicMessageService(ALERTS);
  }

  @Bean
  public TopicMessageService metricsMessageService() {
    return new TopicMessageService(METRICS);
  }

}

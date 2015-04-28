/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist.config;

import io.fstream.core.config.CoreConfig;
import io.fstream.core.model.event.AlertEvent;
import io.fstream.core.model.event.MetricEvent;
import io.fstream.core.model.event.TickEvent;
import io.fstream.core.model.topic.Topic;
import io.fstream.persist.service.KafkaService;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties
public class PersistConfig extends CoreConfig {

  @Bean
  public KafkaService ratesService() {
    return new KafkaService(Topic.RATES, TickEvent.class);
  }

  @Bean
  public KafkaService alertsService() {
    return new KafkaService(Topic.ALERTS, AlertEvent.class);
  }

  @Bean
  public KafkaService metricsService() {
    return new KafkaService(Topic.METRICS, MetricEvent.class);
  }

}

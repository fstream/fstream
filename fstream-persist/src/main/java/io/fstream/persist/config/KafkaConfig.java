/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist.config;

import io.fstream.core.model.event.AlertEvent;
import io.fstream.core.model.event.MetricEvent;
import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Quote;
import io.fstream.core.model.event.Snapshot;
import io.fstream.core.model.event.Trade;
import io.fstream.core.model.topic.Topic;
import io.fstream.persist.kafka.KafkaService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("kafka")
public class KafkaConfig {

  @Bean
  public KafkaService tradesService() {
    return new KafkaService(Topic.TRADES, Trade.class);
  }

  @Bean
  public KafkaService ordersService() {
    return new KafkaService(Topic.ORDERS, Order.class);
  }

  @Bean
  public KafkaService snapshotsService() {
    return new KafkaService(Topic.SNAPSHOTS, Snapshot.class);
  }

  @Bean
  public KafkaService quotesService() {
    return new KafkaService(Topic.QUOTES, Quote.class);
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

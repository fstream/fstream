/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.config;

import io.fstream.core.model.event.TickEvent;
import io.fstream.rates.camel.TickEventTypeConverter;

import java.util.concurrent.TimeUnit;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import quickfix.fix44.MarketDataSnapshotFullRefresh;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;

/**
 * Java config for Spring consumption.
 */
@Slf4j
@Configuration
public class CamelConfig {

  @Bean
  public CamelContextConfiguration contextConfiguration() {
    return new CamelContextConfiguration() {

      @Override
      public void beforeApplicationStart(CamelContext context) {
        // Define mapping from FIX to fStream objects
        context.getTypeConverterRegistry().addTypeConverter(
            TickEvent.class, MarketDataSnapshotFullRefresh.class, tickEventTypeConverter());
      }

    };
  }

  @Bean
  public MetricRegistry metricRegistry() {
    val registry = new MetricRegistry();
    val reporter = Slf4jReporter.forRegistry(registry)
        .outputTo(log)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build();
    reporter.start(60, TimeUnit.SECONDS);

    return registry;
  }

  @Bean
  public TickEventTypeConverter tickEventTypeConverter() {
    return new TickEventTypeConverter();
  }

}

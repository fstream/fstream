/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.config;

import io.fstream.core.model.event.QuoteEvent;
import io.fstream.rates.util.TickEventTypeConverter;

import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import quickfix.fix44.MarketDataSnapshotFullRefresh;

/**
 * Java config for Spring consumption.
 */
@Configuration
public class CamelConfig {

  @Bean
  public CamelContextConfiguration contextConfiguration() {
    return new CamelContextConfiguration() {

      @Override
      public void beforeApplicationStart(CamelContext context) {
        // Define mapping from FIX to fStream objects
        context.getTypeConverterRegistry().addTypeConverter(
            QuoteEvent.class, MarketDataSnapshotFullRefresh.class, tickEventTypeConverter());
      }

    };
  }

  @Bean
  public TickEventTypeConverter tickEventTypeConverter() {
    return new TickEventTypeConverter();
  }

}

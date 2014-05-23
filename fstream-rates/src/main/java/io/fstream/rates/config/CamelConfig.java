/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.config;

import static io.fstream.rates.camel.PropertiesComponents.newPropertiesComponent;
import io.fstream.core.model.event.TickEvent;
import io.fstream.rates.camel.TickEventTypeConverter;

import org.apache.camel.CamelContext;
import org.apache.camel.spring.javaconfig.CamelConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import quickfix.fix44.MarketDataSnapshotFullRefresh;

/**
 * Java config for Spring consumption.
 */
@Configuration
public class CamelConfig extends CamelConfiguration {

  /**
   * Constants.
   */
  private static final String CAMEL_PROPERTIES_NAME = "properties";

  /**
   * Runtime properties.
   */
  @Autowired
  private Environment environment;

  @Bean
  public TickEventTypeConverter rateTypeConverter() {
    return new TickEventTypeConverter();
  }

  @Override
  protected void setupCamelContext(CamelContext camelContext) throws Exception {
    // Make all Spring properties available to Camel
    camelContext.addComponent(CAMEL_PROPERTIES_NAME, newPropertiesComponent(environment));

    // Define mapping from FIX to fStream objects
    camelContext.getTypeConverterRegistry().addTypeConverter(
        TickEvent.class, MarketDataSnapshotFullRefresh.class, rateTypeConverter());
  }

}

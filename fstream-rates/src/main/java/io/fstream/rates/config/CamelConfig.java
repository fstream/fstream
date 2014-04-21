/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.config;

import static io.fstream.rates.util.PropertiesComponents.newPropertiesComponent;
import io.fstream.core.model.Rate;
import io.fstream.rates.handler.RateTypeConverter;

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

  @Autowired
  private Environment environment;

  @Bean
  public RateTypeConverter rateTypeConverter() {
    return new RateTypeConverter();
  }

  @Override
  protected void setupCamelContext(CamelContext camelContext) throws Exception {
    camelContext.addComponent("properties", newPropertiesComponent(environment));
    camelContext.getTypeConverterRegistry().addTypeConverter(
        Rate.class, MarketDataSnapshotFullRefresh.class, rateTypeConverter());
  }

}

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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

import quickfix.fix44.MarketDataSnapshotFullRefresh;

/**
 * Java config for Spring consumption.
 */
@Configuration
@ComponentScan("io.fstream.rates")
@PropertySource("classpath:fstream.properties")
public class Config extends CamelConfiguration {

  @Autowired
  private Environment environment;

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Override
  protected void setupCamelContext(CamelContext camelContext) throws Exception {
    camelContext.addComponent("properties", newPropertiesComponent(environment));
    camelContext.getTypeConverterRegistry().addTypeConverter(
        Rate.class, MarketDataSnapshotFullRefresh.class, new RateTypeConverter());
  }

}

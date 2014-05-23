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
import io.fstream.core.model.event.TickEvent;
import io.fstream.rates.handler.TickEventTypeConverter;
import lombok.val;

import org.apache.camel.CamelContext;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.impl.DefaultDataFormatResolver;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.spring.javaconfig.CamelConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import quickfix.fix44.MarketDataSnapshotFullRefresh;

import com.fasterxml.jackson.datatype.joda.JodaModule;

/**
 * Java config for Spring consumption.
 */
@Configuration
public class CamelConfig extends CamelConfiguration {

  /**
   * Constants.
   */
  private static final String CAMEL_JSON_JACKSON_DATA_FORMAT = "json-jackson";
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

  @Bean
  public JacksonDataFormat jacksonDataFormat() {
    val dataFormat = new JacksonDataFormat();
    dataFormat.setUnmarshalType(TickEvent.class);

    val mapper = dataFormat.getObjectMapper();
    mapper.registerModule(new JodaModule());

    return dataFormat;
  }

  @Override
  protected void setupCamelContext(CamelContext camelContext) throws Exception {

    camelContext.addComponent(CAMEL_PROPERTIES_NAME, newPropertiesComponent(environment));

    val jacksonDataFormat = jacksonDataFormat();
    camelContext.setDataFormatResolver(new DefaultDataFormatResolver() {

      @Override
      public DataFormat resolveDataFormat(String name, CamelContext context) {
        val target = name.equals(CAMEL_JSON_JACKSON_DATA_FORMAT);
        if (target) {
          // Override
          return jacksonDataFormat;
        } else {
          // Delgate
          return super.resolveDataFormat(name, context);
        }
      }

    });

    camelContext.getTypeConverterRegistry().addTypeConverter(
        TickEvent.class, MarketDataSnapshotFullRefresh.class, rateTypeConverter());
  }

}

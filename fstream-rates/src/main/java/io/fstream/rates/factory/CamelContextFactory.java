/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.factory;

import static io.fstream.core.util.FormatUtils._;
import static lombok.AccessLevel.PRIVATE;
import io.fstream.core.model.Rate;
import io.fstream.rates.handler.FixMessageLogger;
import io.fstream.rates.handler.RateTypeConverter;
import io.fstream.rates.routes.OandaRoutes;

import java.util.List;
import java.util.Properties;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import org.apache.camel.CamelContext;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.spi.Registry;

import quickfix.fix44.MarketDataSnapshotFullRefresh;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;

/**
 * Factory for context creation.
 */
@NoArgsConstructor(access = PRIVATE)
public final class CamelContextFactory {

  /**
   * Name of the configuration file.
   */
  private static final String PROPERTIES_FILE = "fstream.properties";

  public static CamelContext newContext() throws Exception {
    // Get configuration
    val properties = getProperties();

    // Create base context
    val context = new DefaultCamelContext(newRegistry());
    context.addComponent("properties", newPropertiesComponent());

    // Configure
    context.addRoutes(new OandaRoutes(getList(properties, "oanda.rates.symbols")));
    // context.addRoutes(new StubRoutes());

    context.getTypeConverterRegistry().addTypeConverter(
        Rate.class, MarketDataSnapshotFullRefresh.class, new RateTypeConverter());

    return context;
  }

  private static Registry newRegistry() {
    // Register beans for use with bean uri
    val registry = new SimpleRegistry();
    registry.put("fixMessageLogger", new FixMessageLogger());

    return registry;
  }

  private static PropertiesComponent newPropertiesComponent() {
    return new PropertiesComponent(_("classpath:%s", PROPERTIES_FILE));
  }

  private static List<String> getList(Properties properties, String propertyName) {
    val value = properties.getProperty(propertyName);
    val iterable = Splitter.on(',').omitEmptyStrings().trimResults().split(value);

    return ImmutableList.copyOf(iterable);
  }

  @SneakyThrows
  private static Properties getProperties() {
    val properties = new Properties();
    properties.load(Resources.getResource(PROPERTIES_FILE).openConnection().getInputStream());

    return properties;
  }

}

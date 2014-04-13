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
import io.fstream.rates.routes.StubRoutes;
import lombok.NoArgsConstructor;
import lombok.val;

import org.apache.camel.CamelContext;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.spi.Registry;

import quickfix.fix44.MarketDataSnapshotFullRefresh;

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
    val context = new DefaultCamelContext(newRegistry());

    // Configure
    // context.addRoutes(new OandaRoutes());
    context.addRoutes(new StubRoutes());
    context.addComponent("properties", newPropertiesComponent());
    context.getTypeConverterRegistry().addTypeConverter(
        Rate.class, MarketDataSnapshotFullRefresh.class, new RateTypeConverter());

    return context;
  }

  private static Registry newRegistry() {
    // Register beans for use with bean uri (e.g bean:ratesLogger)
    val registry = new SimpleRegistry();
    registry.put("messageLogger", new FixMessageLogger());

    return registry;
  }

  private static PropertiesComponent newPropertiesComponent() {
    return new PropertiesComponent(_("classpath:%s", PROPERTIES_FILE));
  }

}

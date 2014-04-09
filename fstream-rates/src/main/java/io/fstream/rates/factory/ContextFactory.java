/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.factory;

import static lombok.AccessLevel.PRIVATE;
import io.fstream.rates.handler.RatesLogger;
import io.fstream.rates.routes.OandaRoutes;
import lombok.NoArgsConstructor;
import lombok.val;

import org.apache.camel.CamelContext;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.spi.Registry;

/**
 * Factory for context creation.
 */
@NoArgsConstructor(access = PRIVATE)
public final class ContextFactory {

  public static CamelContext newContext() throws Exception {
    // Create the context with routes, sourcing the properties from the classpath
    val context = new DefaultCamelContext(newRegistry());
    context.addRoutes(new OandaRoutes());
    context.addComponent("properties", new PropertiesComponent("classpath:fstream.properties"));

    return context;
  }

  private static Registry newRegistry() {
    // Register beans for use with beans uri
    val registry = new SimpleRegistry();
    registry.put("ratesLogger", new RatesLogger());

    return registry;
  }

}

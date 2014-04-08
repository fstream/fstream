/* 
 * Copyright (c) 2014 fStream. All Rights Reserved.
 * 
 * Project and contact information: https://bitbucket.org/fstream/fstream
 * 
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package io.fstream.rates;

import static java.lang.System.in;
import static java.lang.System.out;
import io.fstream.rates.handler.RatesLogger;
import io.fstream.rates.routes.OandaRouteBuilder;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.camel.CamelContext;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.spi.Registry;

@Slf4j
public class Main {

  public static void main(String... args) throws Exception {
    new Main().run();
  }

  public void run() throws Exception {
    log.info("Creating Camel context...");
    val context = createContext();
    log.info("Created Camel context");

    log.info("Starting Camel context...");
    context.start();
    log.info("Started Camel context");

    try {
      out.println("\n\n*** Press enter to stop application\n\n");
      in.read();
    } finally {
      log.info("Stopping Camel context...");
      context.stop();
      log.info("Stopped Camel context");
    }
  }

  private CamelContext createContext() throws Exception {
    // Create the context with routes, sourcing the properties from the classpath
    val context = new DefaultCamelContext(createRegistry());
    context.addRoutes(new OandaRouteBuilder());
    context.addComponent("properties", new PropertiesComponent("classpath:fstream.properties"));

    return context;
  }

  private Registry createRegistry() {
    // Register beans
    val registry = new SimpleRegistry();
    registry.put("ratesLogger", new RatesLogger());

    return registry;
  }

}
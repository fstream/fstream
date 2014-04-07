/* 
 * Copyright (c) 2014 fStream. All Rights Reserved.
 * 
 * Project and contact information: https://bitbucket.org/fstream/fstream
 * 
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package io.fstream.rates;

import io.fstream.rates.routes.OandaRouteBuilder;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.camel.CamelContext;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;

@Slf4j
public class Main {

  public static void main(String... args) throws Exception {
    new Main().run();
  }

  public void run() throws Exception {
    log.info("Starting Camel context");
    val context = createContext();
    context.start();

    try {
      log.info("\n\n*** Press enter to stop application\n\n");
      System.in.read();
    } finally {
      log.info("Stopping Camel context...");
      context.stop();
    }
  }

  private CamelContext createContext() throws Exception {
    // Create the context with routes and sourcing the properties from the classpath
    val context = new DefaultCamelContext();
    context.addRoutes(new OandaRouteBuilder());
    context.addComponent("properties", new PropertiesComponent("classpath:fstream.properties"));

    return context;
  }

}
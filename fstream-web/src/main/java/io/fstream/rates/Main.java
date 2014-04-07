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
import org.apache.camel.builder.RouteBuilder;
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
      log.info("*** Press enter to stop application");
      System.in.read();
    } finally {
      log.info("Stopping Camel context...");
      context.stop();
    }
  }

  private CamelContext createContext() throws Exception {
    val context = new DefaultCamelContext();
    context.addRoutes(createRoutes());
    context.addComponent("properties", new PropertiesComponent("classpath:fstream.properties"));

    return context;
  }

  private RouteBuilder createRoutes() {
    return new OandaRouteBuilder("quickfix:oanda-fxpractice.cfg?sessionID=FIX.4.4:baijud->OANDA/RATES");
  }

}
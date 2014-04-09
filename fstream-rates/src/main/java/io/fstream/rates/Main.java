/* 
 * Copyright (c) 2014 fStream. All Rights Reserved.
 * 
 * Project and contact information: https://bitbucket.org/fstream/fstream
 * 
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package io.fstream.rates;

import static io.fstream.rates.factory.ContextFactory.newContext;
import static java.lang.System.in;
import static java.lang.System.out;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * Application entry point.
 */
@Slf4j
public class Main {

  public static void main(String... args) throws Exception {
    new Main().run();
  }

  public void run() throws Exception {
    log.info("Creating Camel context...");
    val context = newContext();
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

}
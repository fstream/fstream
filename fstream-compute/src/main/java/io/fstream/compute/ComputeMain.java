/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package io.fstream.compute;

import static java.lang.System.out;
import io.fstream.core.config.Main;

import org.springframework.boot.SpringApplication;

/**
 * Application entry point.
 */
@Main
public class ComputeMain {

  public static void main(String... args) throws Exception {
    SpringApplication.run(ComputeMain.class, args);

    out.println("\n\n*** Running compute. Press CTLR+C to shutdown\n\n");

    // TODO: daemonize
    Thread.sleep(Long.MAX_VALUE);
  }

}

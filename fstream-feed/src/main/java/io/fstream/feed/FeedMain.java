/* 
 * Copyright (c) 2014 fStream. All Rights Reserved.
 * 
 * Project and contact information: https://bitbucket.org/fstream/fstream
 * 
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package io.fstream.feed;

import static java.lang.System.out;
import io.fstream.core.config.Main;

import org.springframework.boot.SpringApplication;

/**
 * Application entry point.
 */
@Main
public class FeedMain {

  public static void main(String... args) throws Exception {
    SpringApplication.run(FeedMain.class, args);

    out.println("\n\n*** Running feed. Press CTLR+C to shutdown\n\n");

    // TODO: daemonize
    Thread.sleep(Long.MAX_VALUE);
  }

}
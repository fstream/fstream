/* 
 * Copyright (c) 2014 fStream. All Rights Reserved.
 * 
 * Project and contact information: https://bitbucket.org/fstream/fstream
 * 
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package io.fstream.web;

import static java.lang.System.out;
import io.fstream.core.config.Main;

import org.springframework.boot.SpringApplication;

/**
 * Application entry point.
 */
@Main
public class WebMain {

  public static void main(String... args) throws Exception {
    SpringApplication.run(WebMain.class, args);

    out.println("\n\n*** Running web. Press CTLR+C to shutdown\n\n");
  }

}
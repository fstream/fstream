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
import io.fstream.web.config.WebConfig;

import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Application entry point.
 */
public class WebMain {

  public static void main(String... args) throws Exception {
    new SpringApplicationBuilder()
        .sources(WebConfig.class)
        .run(args);

    out.println("\n\n*** Running web. Press CTLR+C to shutdown\n\n");
    Thread.sleep(Long.MAX_VALUE);
  }

}
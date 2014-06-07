/* 
 * Copyright (c) 2014 fStream. All Rights Reserved.
 * 
 * Project and contact information: https://bitbucket.org/fstream/fstream
 * 
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package io.fstream.rates;

import static java.lang.System.out;
import io.fstream.rates.config.RatesConfig;

import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Application entry point.
 */
public class RatesMain {

  public static void main(String... args) throws Exception {
    new SpringApplicationBuilder()
        .sources(RatesConfig.class)
        .run(args);

    out.println("\n\n*** Running rates. Press CTLR+C to shutdown\n\n");
    Thread.sleep(Long.MAX_VALUE);
  }

}
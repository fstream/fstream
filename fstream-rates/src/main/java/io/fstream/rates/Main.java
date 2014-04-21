/* 
 * Copyright (c) 2014 fStream. All Rights Reserved.
 * 
 * Project and contact information: https://bitbucket.org/fstream/fstream
 * 
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package io.fstream.rates;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.readLines;
import static java.lang.System.in;
import static java.lang.System.out;
import static joptsimple.internal.Strings.repeat;
import io.fstream.rates.config.Config;

import java.io.IOException;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Application entry point.
 */
@Slf4j
public class Main {

  public static void main(String... args) throws Exception {
    logBanner();

    new SpringApplicationBuilder()
        .showBanner(false)
        .sources(Config.class)
        .run(args);

    out.println("\n\n*** Running rates. Press any key to shutdown\n\n");
    in.read();
  }

  private static void logBanner() throws IOException {
    log.info("{}", repeat('-', 100));
    for (val line : readLines(getResource("banner.txt"), UTF_8)) {
      log.info(line);
    }
    log.info("{}", repeat('-', 100));
  }

}
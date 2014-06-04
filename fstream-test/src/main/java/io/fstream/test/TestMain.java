/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.test;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Strings.repeat;
import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.readLines;
import static java.lang.System.out;
import io.fstream.test.config.TestConfig;

import java.io.IOException;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Application entry point.
 */
@Slf4j
public class TestMain {

  public static void main(String... args) throws Exception {
    logBanner();

    new SpringApplicationBuilder()
        .showBanner(false)
        .sources(TestConfig.class)
        .run(args);

    out.println("\n\n*** Running test. Press CTLR+C to shutdown\n\n");
    Thread.sleep(Long.MAX_VALUE);
  }

  private static void logBanner() throws IOException {
    log.info("{}", repeat("-", 100));
    for (val line : readLines(getResource("banner.txt"), UTF_8)) {
      log.info(line);
    }
    log.info("{}", repeat("-", 100));
  }

}

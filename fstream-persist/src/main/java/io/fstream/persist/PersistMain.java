/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package io.fstream.persist;

import static java.lang.System.out;
import io.fstream.persist.config.PersistConfig;

import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Application entry point.
 */
public class PersistMain {

  public static void main(String[] args) throws Exception {
    new SpringApplicationBuilder()
        .sources(PersistConfig.class)
        .run(args);

    out.println("\n\n*** Running persist. Press CTLR+C to shutdown\n\n");
    Thread.sleep(Long.MAX_VALUE);
  }

}

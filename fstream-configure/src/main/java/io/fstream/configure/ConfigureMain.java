/* 
 * Copyright (c) 2014 fStream. All Rights Reserved.
 * 
 * Project and contact information: https://bitbucket.org/fstream/fstream
 * 
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package io.fstream.configure;

import static java.lang.System.out;
import io.fstream.configure.config.ConfigureConfig;

import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Application entry point.
 */
public class ConfigureMain {

  public static void main(String... args) throws Exception {
    new SpringApplicationBuilder()
        .sources(ConfigureConfig.class)
        .run(args);

    out.println("\n\n*** Running configure. Press CTLR+C to shutdown\n\n");
  }

}
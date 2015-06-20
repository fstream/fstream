/* 
 * Copyright (c) 2014 fStream. All Rights Reserved.
 * 
 * Project and contact information: https://bitbucket.org/fstream/fstream
 * 
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package io.fstream.simulate;

import static com.google.common.base.Strings.repeat;
import io.fstream.simulate.core.Simulator;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 */
@Slf4j
@SpringBootApplication
public class SimulateMain implements CommandLineRunner {

  @Autowired
  Simulator simulator;

  public static void main(String... args) throws Exception {
    SpringApplication.run(SimulateMain.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    log.info(repeat("-", 100));
    log.info("Running simulation");
    log.info(repeat("-", 100));

    simulator.simulate();
  }

}
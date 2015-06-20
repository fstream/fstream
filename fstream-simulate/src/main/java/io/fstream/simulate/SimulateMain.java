/* 
 * Copyright (c) 2014 fStream. All Rights Reserved.
 * 
 * Project and contact information: https://bitbucket.org/fstream/fstream
 * 
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package io.fstream.simulate;

import static java.lang.System.out;
import io.fstream.simulate.core.Simulator;
import lombok.val;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 */
@SpringBootApplication
public class SimulateMain implements CommandLineRunner {

  @Autowired(required = false)
  Simulator simulator;

  public static void main(String... args) throws Exception {
    SpringApplication.run(SimulateMain.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    out.println("\n\n*** Running rates. Press CTLR+C to shutdown\n\n");

    val toq = simulator != null;
    if (toq) {
      simulator.simulate();
    }
  }

}
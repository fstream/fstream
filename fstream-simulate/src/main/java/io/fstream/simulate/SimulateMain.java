/* 
 * Copyright (c) 2014 fStream. All Rights Reserved.
 * 
 * Project and contact information: https://bitbucket.org/fstream/fstream
 * 
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package io.fstream.simulate;

import io.fstream.simulate.core.Simulator;
import lombok.val;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 */
@SpringBootApplication
public class SimulateMain {

  public static void main(String... args) throws Exception {
    val context = SpringApplication.run(SimulateMain.class, args);
    val simulator = context.getBean(Simulator.class);

    simulator.simulate();
  }

}
/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.actor.agent;

import static io.fstream.simulate.actor.agent.AgentType.INSTITUTIONAL;
import io.fstream.simulate.config.SimulateProperties;

/**
 * Simulates an institutional participant. The participants trades in larger sizes. Other behaviors such as propensity
 * to buy/sell can be determined from configuration file
 */
public class InstitutionalAgent extends ActiveAgent {

  public InstitutionalAgent(SimulateProperties properties, String name) {
    super(properties, INSTITUTIONAL, name);
  }

}

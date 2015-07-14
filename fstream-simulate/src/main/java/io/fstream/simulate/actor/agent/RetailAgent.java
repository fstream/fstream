/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.actor.agent;

import static io.fstream.simulate.actor.agent.AgentType.RETAIL;
import io.fstream.simulate.config.SimulateProperties;

/**
 * Simulates an retail participant. The participants trades in smaller sizes. Other behaviors such as propensity to
 * buy/sell can be determined from configuration file
 */
public class RetailAgent extends ActiveAgent {

  public RetailAgent(SimulateProperties properties, String name) {
    super(properties, RETAIL, name);
  }

}

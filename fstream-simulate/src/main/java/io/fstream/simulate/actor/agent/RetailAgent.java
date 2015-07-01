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

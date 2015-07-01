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

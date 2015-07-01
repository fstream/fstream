package io.fstream.simulate.actor.agent;

public interface Agent {

  void executeAction();

  enum AgentType {
    RETAIL,
    INSTITUTIONAL,
    HFT;
  }

}

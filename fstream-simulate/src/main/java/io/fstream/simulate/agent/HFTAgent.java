package io.fstream.simulate.agent;

import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.message.SubscriptionQuote;

import org.springframework.beans.factory.annotation.Autowired;

import akka.actor.ActorRef;

public class HFTAgent extends AgentActor {

  @Autowired
  private SimulateProperties properties;

  public HFTAgent(String name, ActorRef exchange) {
    super(name, exchange);
    quoteSubscriptionLevel = properties.getHftProp().getQuoteSubscriptionLevel();
  }

  @Override
  public void executeAction() {

  }

  /*
   * 
   */
  @Override
  public void onReceive(Object message) throws Exception {

  }

  @Override
  public void preStart() {
    // register to recieve quotes
    exchange.tell(new SubscriptionQuote(this.getQuoteSubscriptionLevel()), self());
  }

}

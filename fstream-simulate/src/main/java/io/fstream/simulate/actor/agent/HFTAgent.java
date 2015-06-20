package io.fstream.simulate.actor.agent;

import io.fstream.simulate.message.SubscriptionQuote;
import akka.actor.ActorRef;

public class HFTAgent extends AgentActor {

  public HFTAgent(String name, ActorRef exchange) {
    super(name, exchange);
    quoteSubscriptionLevel = properties.getHftProp().getQuoteSubscriptionLevel();
  }

  @Override
  public void executeAction() {
    // No-op
  }

  @Override
  public void onReceive(Object message) throws Exception {
    // No-op
  }

  @Override
  public void preStart() {
    // Register to receive quotes
    exchange.tell(new SubscriptionQuote(this.getQuoteSubscriptionLevel()), self());
  }

}

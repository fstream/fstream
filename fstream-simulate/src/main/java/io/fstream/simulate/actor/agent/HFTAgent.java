package io.fstream.simulate.actor.agent;

import io.fstream.simulate.message.SubscriptionQuote;
import io.fstream.simulate.util.PrototypeActor;

import org.springframework.context.annotation.Profile;

import akka.actor.ActorRef;

@Profile("hft")
@PrototypeActor
public class HFTAgent extends AgentActor {

  public HFTAgent(String name, ActorRef exchange) {
    super(name, exchange);
    quoteSubscriptionLevel = properties.getHft().getQuoteSubscriptionLevel();
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

package io.fstream.simulate.book;

import io.fstream.simulate.orders.Trade;
import io.fstream.simulate.publisher.Publisher;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import akka.actor.UntypedActor;

/**
 * Register for all executed trades by all clients.
 * 
 * @author bdevani
 *
 */
@Slf4j
@Getter
@Setter
// This class is useless as Exchange publishes trade. But keep for now.
@Deprecated
@Component
@Scope("prototype")
public class TradeBook extends UntypedActor {

  @Autowired
  private Publisher publisher;

  private void addTrade(Trade trade) {
    log.debug(String.format("Trade register for %s at price %f", trade.getAmount(), trade.getPrice()));
  }

  @Override
  public void onReceive(Object message) throws Exception {
    if (message instanceof Trade) {
      this.addTrade((Trade) message);
    }
    else {
      unhandled(message);
    }

  }
}

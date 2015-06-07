package io.fstream.simulate.book;

import io.fstream.simulate.message.Messages;
import io.fstream.simulate.orders.Trade;

import java.util.ArrayList;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
@Component
@Scope("prototype")
public class TradeBook extends UntypedActor {

  private static ArrayList<Trade> TRADES = new ArrayList<Trade>();

  private void addTrade(Trade trade) {
    log.info(String.format("Trade register for %s at price %f", trade.getAmount(), trade.getPrice()));
    TRADES.add(trade);
  }

  public void printTape() {
    for (Trade trade : TRADES) {
      System.out.println(trade.toString());
    }
  }

  @Override
  public void onReceive(Object message) throws Exception {
    if (message instanceof Trade) {
      this.addTrade((Trade) message);
    } else if (message instanceof String) {
      message = (String) message;
      if (message.equals(Messages.PRINT_TRADE_BOOK)) {
        this.printTape();
      }
    } else {
      unhandled(message);
    }

  }
}

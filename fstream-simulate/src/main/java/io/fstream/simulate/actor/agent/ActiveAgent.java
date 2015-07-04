package io.fstream.simulate.actor.agent;

import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Order.OrderSide;
import io.fstream.core.model.event.Order.OrderType;
import io.fstream.core.model.event.Quote;
import io.fstream.simulate.actor.Exchange;
import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.message.ActiveInstruments;
import io.fstream.simulate.message.Command;
import io.fstream.simulate.message.SubscriptionQuoteRequest;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * Agent that actively makes trades.
 */
@Slf4j
public abstract class ActiveAgent extends Agent {

  public ActiveAgent(SimulateProperties properties, AgentType type, String name) {
    super(properties, type, name);
  }

  @Override
  public void preStart() {
    // Trigger "active" behavior
    scheduleSelfOnceRandom(Command.AGENT_EXECUTE_ACTION);
  }

  @Override
  public void onReceive(Object message) throws Exception {
    log.debug("{} message received: {}", name, message);

    if (message instanceof Command) {
      onReceiveCommand((Command) message);
    } else if (message instanceof ActiveInstruments) {
      onReceiveActiveInstruments((ActiveInstruments) message);
    } else if (message instanceof SubscriptionQuoteRequest) {
      onReceiveSubscriptionQuote((SubscriptionQuoteRequest) message);
    } else if (message instanceof Quote) {
      onReceiveQuote((Quote) message);
    } else {
      unhandled(message);
    }
  }

  @Override
  public void executeAction() {
    // "active" behavior"
    val order = createOrder();
    if (order != null) {
      // Cancel all pending open orders on this symbol
      cancelOpenOrdersBySymbol(order.getSymbol());

      // Add the new order
      openOrders.addOpenOrder(order);
      exchange().tell(order, self());
    }
  }

  private Order createOrder() {
    val amount = decideAmount();
    float price;

    if (activeInstruments.getInstruments() == null) {
      // Send a message to exchange and then return null and wait for next decision iteration
      exchange().tell(activeInstruments, self());
      return null;
    }

    OrderType type = decideOrderType(getProbMarket());
    OrderSide side = decideSide(1 - getProbBuy(), OrderSide.ASK);
    val symbol = decideSymbol();

    val quote = this.getLastValidQuote(symbol);
    if (quote == null) {
      log.warn("Empty quote returned by agent {} for symbol {}", name, symbol);
      return null;
    }

    if (type == OrderType.MARKET) {
      if (side == OrderSide.ASK) {
        price = Float.MIN_VALUE;
      } else {
        price = Float.MAX_VALUE;
      }
    } else {
      if (side == OrderSide.ASK) {
        // TODO remove hard coding.
        // max ensures price stays in bounds.
        val bestAsk = quote.getAsk();
        price = decidePrice(bestAsk, Math.min(bestAsk + (minQuoteSize * 5), bestAsk), bestAsk, getProbBestPrice());
      } else {
        // min ensures price doesn't go below some lower bound
        val bestBid = quote.getBid();
        price = decidePrice(Math.max(bestBid - (minQuoteSize * 5), bestBid), bestBid, bestBid, getProbBestPrice());
      }
      if (price < 0) {
        log.error("Invalid price generated {}", price);
      }
    }

    return new Order(side, type, getSimulationTime(), Exchange.nextOrderId(), broker, symbol, amount, price, name);
  }

}

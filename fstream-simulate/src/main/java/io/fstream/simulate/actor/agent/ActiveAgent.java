package io.fstream.simulate.actor.agent;

import io.fstream.simulate.actor.Exchange;
import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.message.ActiveInstruments;
import io.fstream.simulate.message.Command;
import io.fstream.simulate.message.SubscriptionQuoteRequest;
import io.fstream.simulate.model.LimitOrder;
import io.fstream.simulate.model.Order;
import io.fstream.simulate.model.Order.OrderSide;
import io.fstream.simulate.model.Order.OrderType;
import io.fstream.simulate.model.Quote;
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

    val type = decideOrderType(getProbMarket());
    val side = decideSide(1 - getProbBuy(), OrderSide.ASK);
    val symbol = decideSymbol();

    val quote = this.getLastValidQuote(symbol);
    if (quote == null) {
      log.warn("Empty quote returned by agent {} for symbol {}", name, symbol);
      return null;
    }

    if (type == OrderType.MO) {
      if (side == OrderSide.ASK) {
        price = Float.MIN_VALUE;
      } else {
        price = Float.MAX_VALUE;
      }
    } else {
      if (side == OrderSide.ASK) {
        // TODO remove hard coding.
        // max ensures price stays in bounds.
        val bestAsk = quote.getAskPrice();
        price = decidePrice(bestAsk, Math.min(bestAsk + (minTickSize * 5), bestAsk), bestAsk, getProbBestPrice());
      } else {
        // min ensures price doesn't go below some lower bound
        val bestBid = quote.getBidPrice();
        price = decidePrice(Math.max(bestBid - (minTickSize * 5), bestBid), bestBid, bestBid, getProbBestPrice());
      }
      if (price < 0) {
        log.error("Invalid price generated {}", price);
      }
    }

    return new LimitOrder(side, type, getSimulationTime(), Exchange.nextOrderId(), broker, symbol, amount, price, name);
  }

}

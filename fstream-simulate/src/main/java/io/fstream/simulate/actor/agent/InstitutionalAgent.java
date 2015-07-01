package io.fstream.simulate.actor.agent;

import static io.fstream.simulate.actor.agent.Agent.AgentType.INSTITUTIONAL;
import io.fstream.simulate.actor.Exchange;
import io.fstream.simulate.message.ActiveInstruments;
import io.fstream.simulate.message.Command;
import io.fstream.simulate.message.SubscriptionQuote;
import io.fstream.simulate.model.LimitOrder;
import io.fstream.simulate.model.Order;
import io.fstream.simulate.model.Order.OrderSide;
import io.fstream.simulate.model.Order.OrderType;
import io.fstream.simulate.model.Quote;
import io.fstream.simulate.util.PrototypeActor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import akka.actor.ActorRef;

/**
 * Simulates an institutional participant. The participants trades in larger sizes. Other behaviors such as propensity
 * to buy/sell can be determined from configuration file
 */
@Slf4j
@PrototypeActor
public class InstitutionalAgent extends AgentActor {

  public InstitutionalAgent(String name, ActorRef exchange) {
    super(INSTITUTIONAL, name, exchange);
  }

  @Override
  public void preStart() {
    super.preStart();

    scheduleSelfOnceRandom(Command.AGENT_EXECUTE_ACTION);
  }

  @Override
  public void executeAction() {
    val order = createOrder();
    if (order != null) {
      // Cancel all open orders
      cancelAllOpenOrders(order.getSymbol());
      exchange.tell(order, self());
      openOrderBook.addOpenOrder(order);
    }
  }

  @Override
  public void onReceive(Object message) throws Exception {
    log.debug("Agent message received by {}: {}", name, message);
    if (message instanceof Command) {
      onReceiveCommand((Command) message);
    } else if (message instanceof ActiveInstruments) {
      onReceiveActiveInstruments((ActiveInstruments) message);
    } else if (message instanceof SubscriptionQuote) {
      onReceiveSubscriptionQuote((SubscriptionQuote) message);
    } else if (message instanceof Quote) {
      onReceiveQuote((Quote) message);
    } else {
      unhandled(message);
    }
  }

  private Order createOrder() {
    val amount = decideAmount();
    OrderType type = OrderType.ADD;
    OrderSide side;
    float price;

    if (activeInstruments.getInstruments() == null) {
      // Send a message to exchange and then return null and wait for next decision iteration
      exchange.tell(activeInstruments, self());
      return null;
    }

    val symbol = decideSymbol();

    val quote = this.getLastValidQuote(symbol);
    if (quote == null) {
      log.warn("Empty quote returned by agent {} for symbol {}", this.getName(), symbol);
      return null;
    }

    float bestAsk = quote.getAskPrice();
    float bestBid = quote.getBidPrice();

    side = decideSide(1 - probBuy, OrderSide.ASK);
    type = decideOrderType(probMarket);

    if (type == OrderType.MO) {
      if (side == OrderSide.ASK) {
        price = Float.MIN_VALUE;
      } else {
        price = Float.MAX_VALUE;
      }
    } else {
      if (side == OrderSide.ASK) {
        // TODO remove hardcoding.
        // max ensures price stays in bounds.
        price = decidePrice(bestAsk, Math.min(bestAsk + (minTickSize * 5), bestAsk), bestAsk, probBestPrice);
      } else {
        // min ensures price doesn't go below some lower bound
        price = decidePrice(Math.max(bestBid - (minTickSize * 5), bestBid), bestBid, bestBid, probBestPrice);
      }
      if (price < 0) {
        log.error("Invalid price generated {}", price);
      }
    }

    return new LimitOrder(side, type, getSimulationTime(), Exchange.nextOrderId(), broker, symbol, amount, price, name);
  }

}

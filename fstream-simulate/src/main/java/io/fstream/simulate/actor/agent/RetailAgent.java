package io.fstream.simulate.actor.agent;

import static io.fstream.simulate.actor.agent.Agent.AgentType.RETAIL;
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

import org.joda.time.DateTime;

import akka.actor.ActorRef;

/**
 * Simulates an retail participant. The participants trades in smaller sizes. Other behaviors such as propensity to
 * buy/sell can be determined from configuration file
 */
@Slf4j
@PrototypeActor
public class RetailAgent extends AgentActor {

  public RetailAgent(String name, ActorRef exchange) {
    super(RETAIL, name, exchange);
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
      cancelAllOpenOrders(order.getSymbol());
      exchange.tell(order, self());
      openOrderBook.addOpenOrder(order);
    }
  }

  @Override
  public void onReceive(Object message) throws Exception {
    log.debug("Agent message received by {}: {}", this.getName(), message);
    if (message instanceof Command) {
      onReceiveCommand((Command) message);
    } else if (message instanceof ActiveInstruments) {
      onReceiveActiveInstruments((ActiveInstruments) message);
    } else if (message instanceof SubscriptionQuote) {
      onReceiveSubscriptionQuote(((SubscriptionQuote) message));
    } else if (message instanceof Quote) {
      onReceiveQuote((Quote) message);
    } else {
      unhandled(message);
    }
  }

  private Order createOrder() {
    val amount = decideAmount();
    OrderSide side;
    OrderType type = OrderType.ADD;
    float price;

    if (activeInstruments.getInstruments() == null) {
      // send a message to exchange and then return null and wait for next
      // decision iteration
      exchange.tell(activeInstruments, self());
      return null;
    }
    val symbol = decideSymbol();

    val quote = this.getLastValidQuote(symbol);
    if (quote == null) {
      log.warn("Empty quote returned by agent {}", this.getName());
      return null;
    }

    float bestAsk = quote.getAskPrice();
    float bestBid = quote.getBidPrice();

    type = decideOrderType(probMarket);
    side = decideSide(1 - probBuy, OrderSide.ASK);

    if (type == OrderType.MO) {
      if (side == OrderSide.ASK) {
        price = Float.MIN_VALUE;
      } else {
        price = Float.MAX_VALUE;
      }
    } else {
      if (side == OrderSide.ASK) {
        price = decidePrice(bestAsk, Math.min(bestAsk + (minTickSize * 5), bestAsk), bestAsk, probBestPrice);
      } else {
        price = decidePrice(Math.max(bestBid - (minTickSize * 5), bestBid), bestBid, bestBid, probBestPrice);
      }
    }

    return new LimitOrder(side, type, DateTime.now(), Exchange.nextOrderId(), broker, symbol, amount, price, name);
  }

}

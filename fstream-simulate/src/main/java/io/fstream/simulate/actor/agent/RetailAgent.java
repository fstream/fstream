package io.fstream.simulate.actor.agent;

import io.fstream.simulate.actor.Exchange;
import io.fstream.simulate.message.ActiveInstruments;
import io.fstream.simulate.message.Messages;
import io.fstream.simulate.message.SubscriptionQuote;
import io.fstream.simulate.model.LimitOrder;
import io.fstream.simulate.model.Order;
import io.fstream.simulate.model.Order.OrderSide;
import io.fstream.simulate.model.Order.OrderType;
import io.fstream.simulate.model.Quote;
import io.fstream.simulate.util.PrototypeActor;

import javax.annotation.PostConstruct;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.util.Timeout;

/**
 * Simulates an retail participant. The participants trades in smaller sizes. Other behaviors such as propensity to
 * buy/sell can be determined from configuration file
 */
@Slf4j
@PrototypeActor
public class RetailAgent extends AgentActor {

  public RetailAgent(String name, ActorRef exchange) {
    super(name, exchange);
  }

  @PostConstruct
  public void init() {
    maxTradeSize = properties.getRetail().getMaxTradeSize();
    maxSleep = properties.getRetail().getMaxSleep();
    minSleep = properties.getRetail().getMinSleep();

    probMarket = properties.getRetail().getProbMarket();
    probBuy = properties.getRetail().getProbBuy();
    probBestPrice = properties.getRetail().getProbBestPrice();

    quoteSubscriptionLevel = properties.getRetail().getQuoteSubscriptionLevel();

    msgResponseTimeout = new Timeout(Duration.create(properties.getMsgResponseTimeout(), "seconds"));
    minTickSize = properties.getMinTickSize();
    broker = properties.getBrokers().get(random.nextInt(properties.getBrokers().size()));
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

  private Order createOrder() {
    int next = random.nextInt(maxTradeSize);
    int amount = next + 1;
    OrderSide side;
    OrderType type = OrderType.ADD;
    float price;

    if (activeInstruments.getInstruments() == null) {
      // send a message to exchange and then return null and wait for next
      // decision iteration
      exchange.tell(activeInstruments, self());
      return null;
    }
    String symbol =
        activeInstruments.getInstruments().get(random.nextInt(activeInstruments.getInstruments().size()));

    Quote quote = this.getLastValidQuote(symbol);
    if (quote == null) {
      log.warn("empty quote returned by agent {}", this.getName());
      return null;
    }
    float bestask = quote.getAskPrice();
    float bestbid = quote.getBidPrice();

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
        price = decidePrice(bestask, bestask + (minTickSize * 5), bestask, probBestPrice);
      } else {
        price = decidePrice(bestbid - (minTickSize * 5), bestbid, bestbid, probBestPrice);
      }

    }

    return new LimitOrder(side, type, DateTime.now(), Exchange.nextOrderId(), broker, symbol, amount, price, name);
  }

  @Override
  public void onReceive(Object message) throws Exception {
    log.debug("agent message received by " + this.getName() + " " + message.toString());
    if (message instanceof String) {
      if (((String) message).equals(Messages.AGENT_EXECUTE_ACTION)) {
        this.executeAction();
        this.scheduleOnce(Messages.AGENT_EXECUTE_ACTION, generateRandomDuration());
      }
    } else if (message instanceof ActiveInstruments) {
      this.activeInstruments.setInstruments(((ActiveInstruments) message).getInstruments());
    }
    else if (message instanceof SubscriptionQuote) {
      log.debug("agent {} registered successfully to receive level {} quotes", this.getName(),
          this.getQuoteSubscriptionLevel());
      this.quoteSubscriptionSuccess = ((SubscriptionQuote) message).isSuccess();
    }
    else if (message instanceof Quote) {
      this.getBbboQuotes().put(((Quote) message).getSymbol(), (Quote) message);
    } else {
      unhandled(message);
    }
  }

  @Override
  public void preStart() {
    this.scheduleOnce(Messages.AGENT_EXECUTE_ACTION, generateRandomDuration());
    exchange.tell(new SubscriptionQuote(this.getQuoteSubscriptionLevel()), self());
  }

}

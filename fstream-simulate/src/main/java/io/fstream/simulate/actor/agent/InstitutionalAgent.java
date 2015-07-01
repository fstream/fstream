package io.fstream.simulate.actor.agent;

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

import javax.annotation.PostConstruct;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;

import akka.actor.ActorRef;

/**
 * Simulates an institutional participant. The participants trades in larger sizes. Other behaviors such as propensity
 * to buy/sell can be determined from configuration file
 */
@Slf4j
@PrototypeActor
public class InstitutionalAgent extends AgentActor {

  public InstitutionalAgent(String name, ActorRef exchange) {
    super(name, exchange);
  }

  @PostConstruct
  public void init() {
    maxTradeSize = properties.getInstitutional().getMaxTradeSize();
    maxSleep = properties.getInstitutional().getMaxSleep();
    minSleep = properties.getInstitutional().getMinSleep();

    probMarket = properties.getInstitutional().getProbMarket();
    probBuy = properties.getInstitutional().getProbBuy();
    probBestPrice = properties.getInstitutional().getProbBestPrice();

    quoteSubscriptionLevel = properties.getInstitutional().getQuoteSubscriptionLevel();

    minTickSize = properties.getMinTickSize();
    msgResponseTimeout = generateMsgResponseTimeout();
    broker = generateBroker();
  }

  @Override
  public void executeAction() {
    val order = createOrder();
    if (order != null) {
      // cancel all openorders
      cancelAllOpenOrders(order.getSymbol());
      exchange.tell(order, self());
      openOrderBook.addOpenOrder(order);
    }
  }

  private Order createOrder() {
    int next = random.nextInt(maxTradeSize);
    int amount = next + 1;
    OrderType type = OrderType.ADD;
    OrderSide side;
    float price;

    if (activeInstruments.getInstruments() == null) {
      // Send a message to exchange and then return null and wait for next decision iteration
      exchange.tell(activeInstruments, self());
      return null;
    }

    val symbol = activeInstruments.getInstruments().get(random.nextInt(activeInstruments.getInstruments().size()));

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

    return new LimitOrder(side, type, DateTime.now(), Exchange.nextOrderId(), broker, symbol, amount, price, name);
  }

  @Override
  public void onReceive(Object message) throws Exception {
    log.debug("Agent message received by {}: {}", this.getName(), message);
    if (message instanceof Command) {
      if (message.equals(Command.AGENT_EXECUTE_ACTION)) {
        this.executeAction();
        this.scheduleOnceRandom(Command.AGENT_EXECUTE_ACTION);
      }
    } else if (message instanceof ActiveInstruments) {
      this.activeInstruments.setInstruments(((ActiveInstruments) message).getInstruments());
    } else if (message instanceof SubscriptionQuote) {
      log.debug("Agent {} registered successfully to receive level {} quotes", this.getName(),
          this.getQuoteSubscriptionLevel());
      this.quoteSubscriptionSuccess = ((SubscriptionQuote) message).isSuccess();
    } else if (message instanceof Quote) {
      this.getBbboQuotes().put(((Quote) message).getSymbol(), (Quote) message);
    } else {
      unhandled(message);
    }
  }

  @Override
  public void preStart() {
    this.scheduleOnceRandom(Command.AGENT_EXECUTE_ACTION);

    // Register to recieve quotes
    exchange.tell(new SubscriptionQuote(this.getQuoteSubscriptionLevel()), self());
  }

}

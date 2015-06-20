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
    maxTradSize = properties.getInstitutional().getMaxTradeSize();
    maxSleep = properties.getInstitutional().getMaxSleep();
    minSleep = properties.getInstitutional().getMinSleep();

    probMarket = properties.getInstitutional().getProbMarket();
    probBuy = properties.getInstitutional().getProbBuy();
    probBestPrice = properties.getInstitutional().getProbBestPrice();

    quoteSubscriptionLevel = properties.getInstitutional().getQuoteSubscriptionLevel();

    minTickSize = properties.getMinTickSize();
    msgResponseTimeout = new Timeout(Duration.create(properties.getMsgResponseTimeout(), "seconds"));
  }

  @Override
  public void executeAction() {
    val order = createOrder();
    if (order != null) {
      exchange.tell(order, self());
    }
  }

  private Order createOrder() {
    int next = random.nextInt(maxTradSize);
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
      log.warn("Empty quote returned by agent {}", this.getName());
      return null;
    }

    float bestask = quote.getAskprice();
    float bestbid = quote.getBidprice();
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
        price = decidePrice(bestask, Math.min(bestask + (minTickSize * 5), 15), bestask, probBestPrice);
      } else {
        // min ensures price doesn't go below some lower bound
        price = decidePrice(Math.max(bestbid - (minTickSize * 5), 7), bestbid, bestbid, probBestPrice);
      }
      if (price < 0) {
        log.error("Invalid price generated {}", price);
      }
    }

    return new LimitOrder(side, type, DateTime.now(), Exchange.nextOrderId(), "xx", symbol, amount, price, name);
  }

  @Override
  public void onReceive(Object message) throws Exception {
    log.debug("Agent message received by {}: {}", this.getName(), message);
    if (message instanceof String) {
      if (((String) message).equals(Messages.AGENT_EXECUTE_ACTION)) {
        this.executeAction();
        this.scheduleOnce(Messages.AGENT_EXECUTE_ACTION, generateRandomDuration());
      }
    } else if (message instanceof ActiveInstruments) {
      this.activeInstruments.setInstruments(((ActiveInstruments) message).getInstruments());
    } else if (message instanceof SubscriptionQuote) {
      log.debug("agent {} registered successfully to receive level {} quotes", this.getName(),
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
    this.scheduleOnce(Messages.AGENT_EXECUTE_ACTION, generateRandomDuration());

    // Register to recieve quotes
    exchange.tell(new SubscriptionQuote(this.getQuoteSubscriptionLevel()), self());
  }

}

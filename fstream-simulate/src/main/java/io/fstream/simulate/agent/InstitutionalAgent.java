package io.fstream.simulate.agent;

import io.fstream.simulate.message.ActiveInstruments;
import io.fstream.simulate.message.Messages;
import io.fstream.simulate.message.SubscriptionQuote;
import io.fstream.simulate.orders.LimitOrder;
import io.fstream.simulate.orders.Order;
import io.fstream.simulate.orders.Order.OrderSide;
import io.fstream.simulate.orders.Order.OrderType;
import io.fstream.simulate.orders.Positions;
import io.fstream.simulate.orders.Quote;

import javax.annotation.PostConstruct;

import lombok.Getter;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.util.Timeout;

/**
 * Simulates an institutional participant. The participants trades in larger sizes. Other behaviors such as propensity
 * to buy/sell can be determined from configuration file
 */
@Slf4j
@Getter
@Component
@Scope("prototype")
public class InstitutionalAgent extends AgentActor {

  /**
   * Configuration.
   */
  ActiveInstruments activeinstruments = new ActiveInstruments();
  float probMarket;
  float probBuy;
  float probBestPrice;

  /**
   * State.
   */
  Positions positions = new Positions();

  public InstitutionalAgent(String name, ActorRef exchange) {
    super(name, exchange);
  }

  @PostConstruct
  public void init() {
    maxTradSize = properties.getInstitutionalProp().getMaxTradeSize();
    maxSleep = properties.getInstitutionalProp().getMaxSleep();
    minSleep = properties.getInstitutionalProp().getMinSleep();

    probMarket = properties.getInstitutionalProp().getProbMarket();
    probBuy = properties.getInstitutionalProp().getProbBuy();
    probBestPrice = properties.getInstitutionalProp().getProbBestPrice();

    msgResponseTimeout = new Timeout(Duration.create(properties.getMsgResponseTimeout(), "seconds"));
    minTickSize = properties.getMinTickSize();
    quoteSubscriptionLevel = properties.getInstitutionalProp().getQuoteSubscriptionLevel();
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

    if (activeinstruments.getActiveinstruments() == null) {
      // send a message to exchange and then return null and wait for next
      // decision iteration
      exchange.tell(activeinstruments, self());
      return null;
    }
    String symbol =
        activeinstruments.getActiveinstruments().get(random.nextInt(activeinstruments.getActiveinstruments().size()));

    Quote quote = this.getLastValidQuote(symbol);
    if (quote == null) {
      log.warn("empty quote returned by agent {}", this.getName());
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
        log.error("invalid price generated {}", price);
      }

    }
    return new LimitOrder(side, type, DateTime.now(), Exchange.getOID(), "xx", symbol, amount, price, name);
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
      this.activeinstruments.setActiveinstruments(((ActiveInstruments) message).getActiveinstruments());
    }
    else if (message instanceof SubscriptionQuote) {
      log.debug("agent {} registered successfully to receive level {} quotes", this.getName(),
          this.getQuoteSubscriptionLevel());
      this.setQuoteSubscriptionSuccess(((SubscriptionQuote) message).isSuccess());
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
    // register to recieve quotes
    exchange.tell(new SubscriptionQuote(this.getQuoteSubscriptionLevel()), self());
  }

}

package io.fstream.simulate.actor.agent;

import static com.google.common.base.Preconditions.checkState;
import io.fstream.simulate.actor.BaseActor;
import io.fstream.simulate.config.SimulateProperties.AgentProperties;
import io.fstream.simulate.message.ActiveInstruments;
import io.fstream.simulate.message.Command;
import io.fstream.simulate.message.QuoteRequest;
import io.fstream.simulate.message.SubscriptionQuote;
import io.fstream.simulate.model.OpenOrders;
import io.fstream.simulate.model.Order.OrderSide;
import io.fstream.simulate.model.Order.OrderType;
import io.fstream.simulate.model.Quote;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;

@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class AgentActor extends BaseActor implements Agent {

  /**
   * Configuration.
   */
  final AgentType type;
  final String name;

  int maxSleep; // Agent sleep time
  int minSleep;
  int maxTradeSize;
  float minTickSize;
  Timeout msgResponseTimeout;

  float probMarket;
  float probBuy;
  float probBestPrice;

  String quoteSubscriptionLevel;
  boolean quoteSubscriptionSuccess;
  String broker;

  /**
   * Dependencies.
   */
  final ActorRef exchange;

  /**
   * State.
   */
  final Random random = new Random();
  final Map<String, Quote> bbboQuotes = new HashMap<>();
  final OpenOrders openOrderBook = new OpenOrders();

  @Override
  public void preStart() {
    val agentProperties = resolveAgentProperties();

    maxTradeSize = agentProperties.getMaxTradeSize();
    maxSleep = agentProperties.getMaxSleep();
    minSleep = agentProperties.getMinSleep();

    probMarket = agentProperties.getProbMarket();
    probBuy = agentProperties.getProbBuy();
    probBestPrice = agentProperties.getProbBestPrice();

    quoteSubscriptionLevel = agentProperties.getQuoteSubscriptionLevel();

    minTickSize = properties.getMinTickSize();
    msgResponseTimeout = generateMsgResponseTimeout();
    broker = generateBroker();

    // Register to receive quotes
    exchange.tell(new SubscriptionQuote(this.getQuoteSubscriptionLevel()), self());
  }

  /**
   * Template method.
   */
  @Override
  abstract public void executeAction();

  protected void onReceiveSubscriptionQuote(SubscriptionQuote subscriptionQuote) {
    log.debug("Agent {} registered successfully to receive level {} quotes", name, quoteSubscriptionLevel);

    this.quoteSubscriptionSuccess = subscriptionQuote.isSuccess();
  }

  protected void onReceiveActiveInstruments(ActiveInstruments activeInstruments) {
    this.activeInstruments.setInstruments(activeInstruments.getInstruments());
  }

  protected void onReceiveQuote(Quote quote) {
    this.bbboQuotes.put(quote.getSymbol(), quote);
  }

  protected void onReceiveCommand(Command command) {
    if (command == Command.AGENT_EXECUTE_ACTION) {
      this.executeAction();
      this.scheduleSelfOnceRandom(Command.AGENT_EXECUTE_ACTION);
    }
  }

  @NonNull
  protected <T> void scheduleSelfOnceRandom(T message) {
    scheduleSelfOnce(message, generateRandomDuration());
  }

  /**
   * If subscribed successfully read quote. If not received then get market open quote from exchange.
   */
  protected Quote getLastValidQuote(String symbol) {
    Quote quote = this.bbboQuotes.get(symbol);
    if (quote == null) {
      val futureState = Patterns.ask(exchange, new QuoteRequest(symbol), msgResponseTimeout);
      try {
        quote = (Quote) (Await.result(futureState, msgResponseTimeout.duration()));
      } catch (Exception e) {
        log.error("Timeout awaiting state: {}", e.getMessage());
      }
    }

    return quote;
  }

  /**
   * Cancels all open orders for the given symbol
   */
  protected void cancelAllOpenOrders(String symbol) {
    val openOrders = openOrderBook.getOrders().get(symbol);
    for (val openOrder : openOrders) {
      openOrder.setType(OrderType.CANCEL);
      exchange.tell(openOrder, self());
    }

    openOrderBook.getOrders().removeAll(symbol);
  }

  protected String generateBroker() {
    return properties.getBrokers().get(random.nextInt(properties.getBrokers().size()));
  }

  protected Timeout generateMsgResponseTimeout() {
    return new Timeout(Duration.create(properties.getMsgResponseTimeout(), "seconds"));
  }

  /**
   * Generates a random duration between minsleeptime and maxsleeptime;
   */
  protected FiniteDuration generateRandomDuration() {
    return Duration.create(minSleep + random.nextInt(maxSleep - minSleep) + 1, TimeUnit.MILLISECONDS);
  }

  /**
   * Return orderside preferred with the given probability. E.g. prob=0.7, side=BUY returns BUY 70% of the time
   */
  protected OrderSide decideSide(float prob, @NonNull OrderSide side) {
    if (random.nextFloat() <= prob) {
      return side;
    } else {
      if (side == OrderSide.BID) {
        return OrderSide.ASK;
      } else {
        return OrderSide.BID;
      }
    }
  }

  /**
   * With a given probbest will simply return the best price. Otherwise will return a random price within the min/max
   * bounds
   */
  protected float decidePrice(float min, float max, float best, float probBest) {
    if (random.nextFloat() <= probBest) {
      return best;
    } else {
      float price = min + (random.nextFloat() * (max - min));
      return price;
    }
  }

  /**
   * Return a market order with a given probability otherwise limit
   */
  protected OrderType decideOrderType(float probMarket) {
    if (random.nextFloat() <= probMarket) {
      return OrderType.MO;
    } else {
      return OrderType.ADD;
    }
  }

  protected int decideAmount() {
    return random.nextInt(maxTradeSize) + 1;
  }

  protected String decideSymbol() {
    val index = random.nextInt(activeInstruments.getInstruments().size());
    return activeInstruments.getInstruments().get(index);
  }

  private AgentProperties resolveAgentProperties() {
    if (type == AgentType.RETAIL) {
      return properties.getRetail();
    } else if (type == AgentType.INSTITUTIONAL) {
      return properties.getInstitutional();
    } else if (type == AgentType.HFT) {
      return properties.getHft();
    } else {
      checkState(false, "Unexpected agent type '%s'", type);
      return null;
    }

  }

}

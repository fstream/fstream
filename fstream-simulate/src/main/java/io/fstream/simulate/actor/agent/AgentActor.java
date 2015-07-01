package io.fstream.simulate.actor.agent;

import static com.google.common.base.Preconditions.checkState;
import io.fstream.simulate.actor.BaseActor;
import io.fstream.simulate.config.SimulateProperties;
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
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import akka.pattern.Patterns;
import akka.util.Timeout;

@Slf4j
@Getter
public abstract class AgentActor extends BaseActor implements Agent {

  /**
   * Configuration.
   */
  @NonNull
  final AgentType type;
  @NonNull
  final String name;

  int maxSleep;
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
   * State.
   */
  final Random random = new Random();
  final Map<String, Quote> bbboQuotes = new HashMap<>();
  final OpenOrders openOrders = new OpenOrders();

  public AgentActor(SimulateProperties properties, AgentType type, String name) {
    super(properties);

    this.type = type;
    this.name = name;

    // Set agent specific properties
    val agentProperties = resolveAgentProperties();
    this.maxTradeSize = agentProperties.getMaxTradeSize();
    this.maxSleep = agentProperties.getMaxSleep();
    this.minSleep = agentProperties.getMinSleep();

    this.probMarket = agentProperties.getProbMarket();
    this.probBuy = agentProperties.getProbBuy();
    this.probBestPrice = agentProperties.getProbBestPrice();

    this.quoteSubscriptionLevel = agentProperties.getQuoteSubscriptionLevel();

    this.minTickSize = properties.getMinTickSize();
    this.msgResponseTimeout = generateMsgResponseTimeout();
    this.broker = generateBroker();
  }

  @Override
  public void preStart() {
    // Register to receive quotes
    exchange().tell(new SubscriptionQuote(this.getQuoteSubscriptionLevel()), self());
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
      val futureState = Patterns.ask(exchange(), new QuoteRequest(symbol), msgResponseTimeout);
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
    val symbolOrders = openOrders.getOrders().get(symbol);
    for (val symbolOrder : symbolOrders) {
      // FIXME: Sending unsafe mutation of message
      symbolOrder.setType(OrderType.CANCEL);

      exchange().tell(symbolOrder, self());
    }

    openOrders.getOrders().removeAll(symbol);
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

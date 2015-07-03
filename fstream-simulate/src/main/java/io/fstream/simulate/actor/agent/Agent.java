package io.fstream.simulate.actor.agent;

import static com.google.common.base.Preconditions.checkState;
import io.fstream.core.model.event.Order.OrderSide;
import io.fstream.core.model.event.Order.OrderType;
import io.fstream.core.model.event.Quote;
import io.fstream.simulate.actor.BaseActor;
import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.config.SimulateProperties.AgentProperties;
import io.fstream.simulate.message.ActiveInstruments;
import io.fstream.simulate.message.Command;
import io.fstream.simulate.message.QuoteRequest;
import io.fstream.simulate.message.SubscriptionQuoteRequest;
import io.fstream.simulate.model.OpenOrders;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import akka.pattern.Patterns;
import akka.util.Timeout;

@Slf4j
public abstract class Agent extends BaseActor {

  /**
   * Configuration.
   */
  @NonNull
  final AgentType type;
  @NonNull
  final String name;
  @Delegate
  final AgentProperties agentProperties;

  final float minQuoteSize;
  final Timeout msgResponseTimeout;
  final String broker;

  /**
   * State.
   */
  final Random random = new Random();
  final Map<String, Quote> bbboQuotes = new HashMap<>();
  final OpenOrders openOrders = new OpenOrders();

  public Agent(SimulateProperties properties, AgentType type, String name) {
    super(properties);
    this.type = type;
    this.name = name;
    this.agentProperties = resolveAgentProperties();

    this.minQuoteSize = properties.getMinQuoteSize();
    this.msgResponseTimeout = calculateMsgResponseTimeout();
    this.broker = generateBroker();
  }

  @Override
  public void preStart() {
    // Register to receive quotes
    exchange().tell(new SubscriptionQuoteRequest(this.getQuoteSubscriptionLevel()), self());
  }

  /**
   * Template method.
   */
  protected void executeAction() {
    // No-op
  }

  protected void onReceiveSubscriptionQuote(SubscriptionQuoteRequest subscriptionQuote) {
    log.debug("{} registered successfully to receive level {} quotes", name, getQuoteSubscriptionLevel());
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

      // Re-schedule
      this.scheduleSelfOnceRandom(command);
    }
  }

  @NonNull
  protected <T> void scheduleSelfOnceRandom(T message) {
    scheduleSelfOnce(message, generateRandomDuration());
  }

  /**
   * If subscribed successfully, read quote. If not received then get market open quote from exchange.
   */
  protected Quote getLastValidQuote(String symbol) {
    return bbboQuotes.computeIfAbsent(symbol, (key) -> {
      val future = Patterns.ask(exchange(), new QuoteRequest(symbol), msgResponseTimeout);
      try {
        return (Quote) Await.result(future, msgResponseTimeout.duration());
      } catch (Exception e) {
        log.error("Timeout awaiting quote: {}", e.getMessage());
        return null;
      }
    });
  }

  /**
   * Cancels all open orders for the given symbol
   */
  protected void cancelOpenOrdersBySymbol(String symbol) {
    val symbolOrders = openOrders.getOrders().get(symbol);
    for (val symbolOrder : symbolOrders) {
      // FIXME: Sending unsafe mutation of message
      symbolOrder.setOrderType(OrderType.CANCEL);

      exchange().tell(symbolOrder, self());
    }

    openOrders.getOrders().removeAll(symbol);
  }

  protected String generateBroker() {
    return properties.getBrokers().get(random.nextInt(properties.getBrokers().size()));
  }

  protected Timeout calculateMsgResponseTimeout() {
    return new Timeout(Duration.create(properties.getMsgResponseTimeout(), "seconds"));
  }

  /**
   * Generates a random duration between minsleeptime and maxsleeptime;
   */
  protected FiniteDuration generateRandomDuration() {
    return Duration.create(getMinSleep() + random.nextInt(getMaxSleep() - getMinSleep()) + 1, TimeUnit.MILLISECONDS);
  }

  /**
   * Return order side preferred with the given probability. E.g. prob=0.7, side=BUY returns BUY 70% of the time
   */
  protected OrderSide decideSide(float prob, @NonNull OrderSide side) {
    if (random.nextFloat() <= prob) {
      return side;
    } else {
      return side == OrderSide.BID ? OrderSide.ASK : OrderSide.BID;
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
      val price = min + (random.nextFloat() * (max - min));
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

  /**
   * Return a random amount in the range {@code [1, maxTradeSize]}
   */
  protected int decideAmount() {
    return random.nextInt(getMaxTradeSize()) + 1;
  }

  /**
   * Return a random active symbol.
   */
  protected String decideSymbol() {
    val instruments = activeInstruments.getInstruments();
    val index = random.nextInt(instruments.size());
    return instruments.get(index);
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

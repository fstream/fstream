package io.fstream.simulate.actor.agent;

import static com.google.common.base.Preconditions.checkState;
import static io.fstream.core.model.event.Order.OrderSide.ASK;
import static io.fstream.core.model.event.Order.OrderSide.BID;
import static io.fstream.core.model.event.Order.OrderType.LIMIT_ADD;
import static io.fstream.core.model.event.Order.OrderType.LIMIT_CANCEL;
import static io.fstream.core.model.event.Order.OrderType.MARKET_ORDER;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
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
import io.fstream.simulate.util.OpenOrders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.Await;
import scala.concurrent.Future;
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
  final Map<String, Quote> quotes = new HashMap<>();
  final OpenOrders openOrders = new OpenOrders();

  public Agent(SimulateProperties properties, AgentType type, String name) {
    super(properties);
    this.type = type;
    this.name = name;
    this.agentProperties = resolveAgentProperties();

    this.minQuoteSize = properties.getMinQuoteSize();
    this.msgResponseTimeout = new Timeout(Duration.create(properties.getMsgResponseTimeout(), "seconds"));
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
    this.activeInstruments = activeInstruments.getInstruments();
  }

  protected void onReceiveQuote(Quote quote) {
    this.quotes.put(quote.getSymbol(), quote);
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
    scheduleSelfOnce(message, decideRandomDuration());
  }

  /**
   * If subscribed successfully, read quote. If not received then get market open quote from exchange.
   */
  protected Quote getLastValidQuote(String symbol) {
    return quotes.computeIfAbsent(symbol, (key) -> {
      Future<Object> future = Patterns.ask(exchange(), new QuoteRequest(symbol), msgResponseTimeout);
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
      symbolOrder.setOrderType(LIMIT_CANCEL);

      exchange().tell(symbolOrder, self());
    }

    openOrders.getOrders().removeAll(symbol);
  }

  protected String generateBroker() {
    return randomElement(properties.getBrokers());
  }

  /**
   * Generates a random duration between minsleeptime and maxsleeptime;
   */
  protected FiniteDuration decideRandomDuration() {
    return Duration.create(randomInt(getMinSleep(), getMaxSleep()), MILLISECONDS);
  }

  /**
   * With a configured {@code probBestPrice} will simply return the best price. Otherwise will return a random price
   * within the min / max bounds.
   */
  protected float decidePrice(float min, float max, float bestPrice) {
    return randomChoice(getProbBestPrice(), bestPrice, randomFloat(min, max));
  }

  /**
   * Return a market order with a configured {@code probMarket} probability otherwise limit.
   */
  protected OrderType decideOrderType() {
    return randomChoice(getProbMarket(), MARKET_ORDER, LIMIT_ADD);
  }

  /**
   * Return order side preferred with a configured {@code probBuy} probability.
   * <p>
   * e.g. probBuy=0.7, side=BID returns BID 70% of the time
   */
  protected OrderSide decideSide() {
    return randomChoice(getProbBuy(), BID, ASK);
  }

  /**
   * Return a random amount in {@code [1, getMaxTradeSize()]}
   */
  protected int decideAmount() {
    return randomInt(1, getMaxTradeSize());
  }

  /**
   * Return a random active symbol.
   */
  protected String decideSymbol() {
    return randomElement(activeInstruments);
  }

  /**
   * Return a random element in {@code list}.
   */
  protected <T> T randomElement(List<T> list) {
    val randomIndex = random.nextInt(list.size());
    return list.get(randomIndex);
  }

  /**
   * Return a random number in range {@code [min, max]}
   */
  protected int randomInt(int min, int max) {
    return min + random.nextInt(max - min) + 1;
  }

  /**
   * Return a random number in range {@code [min, max]}
   */
  protected float randomFloat(float min, float max) {
    return min + random.nextFloat() * (max - min);
  }

  /**
   * Return {@code a} with {@code aProb}, else {@code b}.
   */
  protected <T> T randomChoice(float aProb, T a, T b) {
    return random.nextFloat() <= aProb ? a : b;
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

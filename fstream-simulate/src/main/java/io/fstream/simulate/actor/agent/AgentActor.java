package io.fstream.simulate.actor.agent;

import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.message.ActiveInstruments;
import io.fstream.simulate.message.QuoteRequest;
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

import org.springframework.beans.factory.annotation.Autowired;

import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import akka.util.Timeout;

@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class AgentActor extends UntypedActor implements Agent {

  /**
   * Configuration.
   */
  final String name;

  int maxSleep; // Agent sleep time
  int minSleep;
  int maxTradSize;
  float minTickSize;
  Timeout msgResponseTimeout;

  float probMarket;
  float probBuy;
  float probBestPrice;

  String quoteSubscriptionLevel;
  boolean quoteSubscriptionSuccess;

  /**
   * Dependencies.
   */
  final ActorRef exchange;
  @Autowired
  SimulateProperties properties;

  /**
   * State.
   */
  ActiveInstruments activeInstruments = new ActiveInstruments();
  Random random = new Random();
  Map<String, Quote> bbboQuotes = new HashMap<>();

  /**
   * Template method.
   */
  @Override
  abstract public void executeAction();

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
  protected float decidePrice(float min, float max, float best, float probbest) {
    if (random.nextFloat() <= probbest) {
      return best;
    } else {
      float price = min + (random.nextFloat() * (max - min));
      return price;
    }
  }

  /**
   * Return a market order with a given probability otherwise limit
   */
  protected OrderType decideOrderType(float probmarket) {
    if (random.nextFloat() <= probmarket) {
      return OrderType.MO;
    } else {
      return OrderType.ADD;
    }
  }

  /**
   * Generates a random duration between minsleeptime and maxsleeptime;
   */
  protected FiniteDuration generateRandomDuration() {
    val duration = Duration.create(random.nextInt(maxSleep - minSleep) + 1, TimeUnit.MILLISECONDS);
    return duration.$plus(Duration.create(minSleep, TimeUnit.MILLISECONDS));
  }

  @NonNull
  protected <T> void scheduleOnce(T message, FiniteDuration duration) {
    val scheduler = getContext().system().scheduler();
    scheduler.scheduleOnce(duration, getSelf(), message, getContext().dispatcher(), null);
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

}

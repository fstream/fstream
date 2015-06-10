package io.fstream.simulate.agent;

import io.fstream.simulate.orders.Order.OrderSide;
import io.fstream.simulate.orders.Order.OrderType;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.util.Timeout;

@Getter
@Setter
public abstract class AgentActor extends UntypedActor implements Agent {

  Random random;
  String name;
  int maxSleep; // agent sleep time
  int minSleep;
  int maxTradSize;
  Timeout msgResponseTimeout;
  protected ActorRef exchange;

  public AgentActor(String name, ActorRef exchange) {
    this.name = name;
    this.exchange = exchange;
  }

  public void init() {
    random = new Random();
  }

  @Override
  abstract public void executeAction();

  /**
   * Return orderside preferred with the given probability. E.g. prob=0.7, side=BUY returns BUY 70% of the time
   * 
   * @param prob
   * @param side
   * @return
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
   * 
   * @param min
   * @param max
   * @param best
   * @param probbest
   * @return
   */
  protected float decidePrice(float min, float max, float best, float probbest) {
    if (random.nextFloat() <= probbest) {
      return best;
    } else {
      return min + (random.nextFloat() * (max - min));
    }
  }

  /**
   * Return a market order with a given probability otherwise limit
   * 
   * @param probmarket
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
   * @return
   */
  protected FiniteDuration generateRandomDuration() {
    FiniteDuration duration = Duration.create(random.nextInt(maxSleep - minSleep) + 1, TimeUnit.MILLISECONDS);
    return duration.$plus(Duration.create(minSleep, TimeUnit.MILLISECONDS));
  }

  protected <T> void scheduleOnce(T message, FiniteDuration duration) {
    getContext().system().scheduler().scheduleOnce(duration, getSelf(), message, getContext().dispatcher(), null);
  }
}

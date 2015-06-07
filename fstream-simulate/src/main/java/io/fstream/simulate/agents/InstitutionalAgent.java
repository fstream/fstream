package io.fstream.simulate.agents;

import io.fstream.simulate.messages.ActiveInstruments;
import io.fstream.simulate.messages.BbBo;
import io.fstream.simulate.messages.Messages;
import io.fstream.simulate.messages.State;
import io.fstream.simulate.orders.Order;
import io.fstream.simulate.orders.Order.OrderSide;
import io.fstream.simulate.orders.Order.OrderType;
import io.fstream.simulate.orders.LimitOrder;
import io.fstream.simulate.orders.Positions;

import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.pattern.Patterns;

@Slf4j
@Getter
@Component
@Scope("prototype")
public class InstitutionalAgent extends AgentActor {

  ActiveInstruments activeinstruments = new ActiveInstruments();

  Positions positions;

  final float PROB_MARKET = 0.30f;
  final float PROB_BUY = 0.49f;
  final float PROB_BESTPRICE = 0.70f;

  public InstitutionalAgent(String name, ActorRef exchange) {
    super(name, exchange);
  }

  @Override
  protected void init() {
    super.init();
    positions = new Positions();
    max_trade_size = 5000;
  }

  @Override
  public void executeAction() {
    Order order = createOrder();
    if (order != null) {
      exchange.tell(order, self());
    }
  }

  private Order createOrder() {
    int next = random.nextInt(max_trade_size);
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
    String symbol = activeinstruments.getActiveinstruments()
        .get(random.nextInt(activeinstruments.getActiveinstruments()
            .size()));

    BbBo bbbo = new BbBo(symbol);
    Future<Object> futurestate = Patterns.ask(exchange, bbbo,
        bookquerytimeout);

    try {
      bbbo = (BbBo) Await
          .result(futurestate, bookquerytimeout.duration());
    } catch (Exception e) {
      log.error("timeout awaiting state");
      return null;
    }

    float bestbid = bbbo.getBestbid() != Float.MIN_VALUE ? bbbo
        .getBestbid() : 8;
    float bestask = bbbo.getBestoffer() != Float.MAX_VALUE ? bbbo
        .getBestoffer() : 10;

    side = decideSide(1 - PROB_BUY, OrderSide.ASK);

    type = decideOrderType(PROB_MARKET);

    if (type == OrderType.MO) {
      if (side == OrderSide.ASK) {
        price = Float.MIN_VALUE;
      } else {
        price = Float.MAX_VALUE;
      }
    } else {
      if (side == OrderSide.ASK) {
        price = decidePrice(bestask, bestask + 5, bestask,
            PROB_BESTPRICE);
      } else {
        price = decidePrice(bestbid - 5, bestbid, bestask,
            PROB_BESTPRICE);
      }

    }
    return new LimitOrder(side, type, DateTime.now(), Exchange.getOID(),
        "xx", symbol, amount, price, name);

  }

  @Override
  public void onReceive(Object message) throws Exception {
    log.debug("agent message received by " + this.getName() + " "
        + message.toString());
    if (message instanceof State) {
      State state = (State) message;
      log.debug("Received state: {}", state);
    } else if (message instanceof String) {
      if (((String) message).equals(Messages.AGENT_EXECUTE_ACTION)) {
        this.executeAction();
        getContext()
            .system()
            .scheduler()
            .scheduleOnce(Duration.create(sleep, TimeUnit.SECONDS),
                getSelf(), Messages.AGENT_EXECUTE_ACTION,
                getContext().dispatcher(), null);
      }
    } else if (message instanceof ActiveInstruments) {
      this.activeinstruments
          .setActiveinstruments(((ActiveInstruments) message)
              .getActiveinstruments());
    } else {
      unhandled(message);
    }

  }

  @Override
  public void preStart() {
    getContext()
        .system()
        .scheduler()
        .scheduleOnce(Duration.create(sleep, TimeUnit.SECONDS),
            getSelf(), Messages.AGENT_EXECUTE_ACTION,
            getContext().dispatcher(), null);
  }

  @Override
  public void postRestart(Throwable reason) {

  }

}

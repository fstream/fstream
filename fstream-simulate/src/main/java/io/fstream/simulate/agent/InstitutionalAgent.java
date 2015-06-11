package io.fstream.simulate.agent;

import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.message.ActiveInstruments;
import io.fstream.simulate.message.BbBo;
import io.fstream.simulate.message.Messages;
import io.fstream.simulate.message.State;
import io.fstream.simulate.orders.LimitOrder;
import io.fstream.simulate.orders.Order;
import io.fstream.simulate.orders.Order.OrderSide;
import io.fstream.simulate.orders.Order.OrderType;
import io.fstream.simulate.orders.Positions;

import javax.annotation.PostConstruct;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;

@Slf4j
@Getter
@Setter
@Component
@Scope("prototype")
public class InstitutionalAgent extends AgentActor {

  @Autowired
  private SimulateProperties properties;

  ActiveInstruments activeinstruments = new ActiveInstruments();

  Positions positions;
  float probMarket;
  float probBuy;
  float probBestPrice;

  public InstitutionalAgent(String name, ActorRef exchange) {
    super(name, exchange);
  }

  @Override
  @PostConstruct
  public void init() {
    super.init();
    positions = new Positions();
    maxTradSize = properties.getInstProp().getMaxTradeSize();
    maxSleep = properties.getRetProp().getMaxSleep();
    minSleep = properties.getRetProp().getMinSleep();
    probMarket = properties.getInstProp().getProbMarket();
    probBuy = properties.getInstProp().getProbBuy();
    probBestPrice = properties.getInstProp().getProbBestPrice();
    msgResponseTimeout = new Timeout(Duration.create(properties.getMsgResponseTimeout(), "seconds"));

  }

  @Override
  public void executeAction() {
    Order order = createOrder();
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

    BbBo bbbo = new BbBo(symbol);
    Future<Object> futurestate = Patterns.ask(exchange, bbbo, msgResponseTimeout);

    try {
      bbbo = (BbBo) Await.result(futurestate, msgResponseTimeout.duration());
    } catch (Exception e) {
      log.error("timeout awaiting state");
      return null;
    }

    float bestbid = bbbo.getBestbid() != Float.MIN_VALUE ? bbbo.getBestbid() : 8;
    float bestask = bbbo.getBestoffer() != Float.MAX_VALUE ? bbbo.getBestoffer() : 10;

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
        price = decidePrice(bestask, bestask + 5, bestask, probBestPrice);
      } else {
        price = decidePrice(bestbid - 5, bestbid, bestask, probBestPrice);
      }

    }
    return new LimitOrder(side, type, DateTime.now(), Exchange.getOID(), "xx", symbol, amount, price, name);

  }

  @Override
  public void onReceive(Object message) throws Exception {
    log.debug("agent message received by " + this.getName() + " " + message.toString());
    if (message instanceof State) {
      State state = (State) message;
      log.debug("Received state: {}", state);
    } else if (message instanceof String) {
      if (((String) message).equals(Messages.AGENT_EXECUTE_ACTION)) {
        this.executeAction();
        this.scheduleOnce(Messages.AGENT_EXECUTE_ACTION, generateRandomDuration());
      }
    } else if (message instanceof ActiveInstruments) {
      this.activeinstruments.setActiveinstruments(((ActiveInstruments) message).getActiveinstruments());
    } else {
      unhandled(message);
    }

  }

  @Override
  public void preStart() {
    this.scheduleOnce(Messages.AGENT_EXECUTE_ACTION, generateRandomDuration());
  }

}

package io.fstream.simulate.actor.agent;

import static io.fstream.core.model.event.Order.OrderSide.ASK;
import static io.fstream.core.model.event.Order.OrderType.MARKET_ORDER;
import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Order.OrderSide;
import io.fstream.core.model.event.Order.OrderType;
import io.fstream.core.model.event.Quote;
import io.fstream.simulate.actor.Exchange;
import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.message.ActiveInstruments;
import io.fstream.simulate.message.Command;
import io.fstream.simulate.message.SubscriptionQuoteRequest;
import lombok.NonNull;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * Agent that actively makes trades.
 */
@Slf4j
public abstract class ActiveAgent extends Agent {

  public ActiveAgent(SimulateProperties properties, AgentType type, String name) {
    super(properties, type, name);
  }

  @Override
  public void preStart() {
    // Trigger "active" behavior
    scheduleSelfOnceRandom(Command.AGENT_EXECUTE_ACTION);
  }

  @Override
  public void onReceive(Object message) throws Exception {
    log.debug("{} message received: {}", name, message);

    if (message instanceof Command) {
      onReceiveCommand((Command) message);
    } else if (message instanceof ActiveInstruments) {
      onReceiveActiveInstruments((ActiveInstruments) message);
    } else if (message instanceof SubscriptionQuoteRequest) {
      onReceiveSubscriptionQuote((SubscriptionQuoteRequest) message);
    } else if (message instanceof Quote) {
      onReceiveQuote((Quote) message);
    } else {
      unhandled(message);
    }
  }

  @Override
  public void executeAction() {
    // "active" behavior"
    val order = createOrder();
    if (order != null) {
      // Cancel all pending open orders on this symbol
      cancelOpenOrdersBySymbol(order.getSymbol());

      // Add the new order
      openOrders.addOpenOrder(order);
      exchange().tell(order, self());
    }
  }

  private Order createOrder() {
    if (activeInstruments.isEmpty()) {
      // Send a message to exchange and then return null and wait for next decision iteration
      exchange().tell(new ActiveInstruments(), self());
      return null;
    }

    val symbol = decideSymbol();
    val quote = getLastQuote(symbol);
    val orderType = decideOrderType();
    val side = decideSide();
    val amount = decideAmount();
    val price = decidePrice(orderType, side, quote);

    return new Order(side, orderType, getSimulationTime(), Exchange.nextOrderId(), broker, symbol, amount, price, name);
  }

  private float decidePrice(OrderType orderType, OrderSide side, @NonNull Quote quote) {
    float price;
    if (orderType == MARKET_ORDER) {
      // TODO: Explain why this is needed if the same calculation is done in OrderBook#onReceiveOrder
      price = side == ASK ? Float.MIN_VALUE : Float.MAX_VALUE;
    } else {
      // TODO: Explain what the 5 is for
      val priceOffset = minQuoteSize * 5;
      if (side == ASK) {
        val bestAsk = quote.getAsk();
        val maxPrice = Math.min(bestAsk + priceOffset, bestAsk);

        price = decidePrice(bestAsk, maxPrice, bestAsk);
      } else {
        val bestBid = quote.getBid();
        val minPrice = Math.max(bestBid - priceOffset, bestBid);

        price = decidePrice(minPrice, bestBid, bestBid);
      }

      if (price < 0) {
        log.error("Invalid price generated {}", price);
      }
    }

    return price;
  }

}

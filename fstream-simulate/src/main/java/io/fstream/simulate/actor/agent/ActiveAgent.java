/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.actor.agent;

import static com.google.common.base.Preconditions.checkState;
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
    super.preStart();

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
      // Add the new order
      if (order.getOrderType() != OrderType.MARKET_ORDER) {
        // Cancel all pending open orders on this symbol
        if (openOrders.getOrders().containsKey(order.getSymbol())) { // if existing order then decide to
                                                                     // cancel/re-insert
                                                                     // with cancelProb
          if (!randomChoice(getProbCancel(), true, false)) {
            return;
          }
        }
      }
      cancelOpenOrdersBySymbol(order.getSymbol());
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
    // represents how far from current price should order be to compensate for risk
    val riskDistance = properties.getRiskDistance();
    val askCeiling = properties.getMaxPrice();
    val bidFloor = properties.getMinPrice();
    if (orderType == MARKET_ORDER) {
      // TODO: Explain why this is needed if the same calculation is done in OrderBook#onReceiveOrder
      price = side == ASK ? Float.MIN_VALUE : Float.MAX_VALUE;
    } else {
      val priceOffset = properties.getTickSize() * randomInt(0, riskDistance) + 1;
      if (side == ASK) {
        val bestAsk = quote.getAsk();
        val maxPrice = bestAsk + priceOffset;

        price = decidePrice(bestAsk, maxPrice, bestAsk);
        // make sure price does go above max bound
        price = price <= askCeiling ? price : askCeiling - (properties.getTickSize() * randomInt(0, riskDistance));
      } else {
        val bestBid = quote.getBid();
        val minPrice = bestBid - priceOffset;
        price = decidePrice(minPrice, bestBid, bestBid);
        // make sure price does not drop below min bound
        price = price >= bidFloor ? price : bidFloor + (properties.getTickSize() * randomInt(0, riskDistance));
      }

      checkState(price >= 0, "Invalid negative price generated %s", price);
    }
    price = Math.round(price * 100) / 100f;
    return price;
  }

}

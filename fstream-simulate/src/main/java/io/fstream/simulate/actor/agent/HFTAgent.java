/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.actor.agent;

import static io.fstream.simulate.actor.agent.AgentType.HFT;
import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Order.OrderSide;
import io.fstream.core.model.event.Order.OrderType;
import io.fstream.core.model.event.Quote;
import io.fstream.simulate.actor.Exchange;
import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.message.ActiveInstruments;
import io.fstream.simulate.message.Command;
import io.fstream.simulate.message.SubscriptionQuoteRequest;
import lombok.Value;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * High frequency trader agent.
 */
@Slf4j
public class HFTAgent extends Agent {

  public HFTAgent(SimulateProperties properties, String name) {
    super(properties, HFT, name);
  }

  @Override
  public void preStart() {
    super.preStart();
    // Trigger "active" behavior
    // scheduleSelfOnceRandom(Command.AGENT_EXECUTE_ACTION);
  }

  @Override
  public void onReceive(Object message) throws Exception {
    if (message instanceof Command) {
      onReceiveCommand((Command) message);
    }
    else if (message instanceof Quote) {
      super.onReceiveQuote((Quote) message);
      onReceiveQuote((Quote) message);
    } else if (message instanceof ActiveInstruments) {
      onReceiveActiveInstruments((ActiveInstruments) message);
    }
    else if (message instanceof SubscriptionQuoteRequest) {
      onReceiveSubscriptionQuote((SubscriptionQuoteRequest) message);
    }
  }

  @Override
  public void executeAction() {
    if (activeInstruments.isEmpty()) {
      // Send a message to exchange and then return null and wait for next decision iteration
      exchange().tell(new ActiveInstruments(), self());
      return;
    }
    Quote quote = getLastQuote(decideSymbol());
    onReceiveQuote(quote);
  }

  @Override
  protected void onReceiveQuote(Quote quote) {
    // if quote is older than 10ms ignore.
    if ((Exchange.getSimulationTime().getMillis() - quote.getDateTime().getMillis()) > 10) {
      return;
    }

    val imbalance = getImbalance(quote);
    val order = isNormal(quote, imbalance) ?
        createLiquidityNormal(quote, imbalance) :
        createLiquidityAtStress(quote, imbalance);
    if (openOrders.getOrders().containsKey(order.getSymbol())) {
      if (!randomChoice(getProbCancel(), true, false)) { // determine whether to cancel/send and order with given
                                                         // probability
        return;
      }
    }

    // Cancel any existing orders in the book
    cancelOpenOrdersBySymbol(order.getSymbol());
    // TODO: For now optimistically assume all LimitOrders sent are accepted by the exchange (no rejects)
    exchange().tell(order, self());
    openOrders.addOpenOrder(order);

  }

  private boolean isNormal(Quote quote, Imbalance imbalance) {
    val spread = quote.getAsk() - quote.getBid();

    return imbalance.getRatio() < 10 && imbalance.getRatio() > 0 && spread > minQuoteSize;
  }

  private Order createLiquidityNormal(Quote quote, Imbalance imbalance) {
    // No stress period, so provide liquidity at better price
    val bestAsk = generateBestAsk(quote);
    val bestBid = generateBestBid(quote);

    val price = decidePrice(imbalance, bestAsk, bestBid);

    return new Order(imbalance.getSide(), OrderType.LIMIT_ADD, getSimulationTime(), Exchange.nextOrderId(), broker,
        quote.getSymbol(), decideAmount(), price, name);
  }

  private float decidePrice(Imbalance imbalance, final float bestAsk, final float bestBid) {
    float price;
    // speeds up how fast prices can narrow to min spread.
    // TODO should be a property if permanent. This is a temp. fix.
    int narrowingIncrement = 3;
    if (imbalance.getSide() == OrderSide.BID) {
      // bid imbalance
      price = bestBid + (minQuoteSize * narrowingIncrement);
      float spread = Math.round((price - bestBid) * 10) / 10.0f;
      if (spread < minQuoteSize) {
        log.error("Invalid spread/ask ask = {}, bid = {}, spread = {}. rejecting", price, bestBid, spread);
        price = bestBid;
      }
    } else {
      // ask imbalance
      price = bestAsk - (minQuoteSize * narrowingIncrement);
      float spread = Math.round((bestAsk - price) * 10) / 10f;
      if (spread < minQuoteSize) {
        log.error("Invalid spread/bid ask = {}, bid = {}, spread = {}. rejecting", bestAsk, price, spread);
        price = bestAsk;
      }
    }
    price = Math.round(price * 100) / 100f;
    return price;
  }

  private Imbalance getImbalance(Quote quote) {
    int imbalanceAmount;
    OrderSide imbalanceSide;
    float imbalanceRatio;

    if (quote.getAskAmount() > quote.getBidAmount()) {
      imbalanceAmount = quote.getAskAmount() - quote.getBidAmount();
      imbalanceSide = OrderSide.BID;
      imbalanceRatio = quote.getBidAmount() != 0 ? quote.getAskAmount() / quote.getBidAmount() : -1;
    } else {
      // TODO: If both sides are 0, then this will bias creation of ASK liquidity. Not a big deal now, but fix later.
      imbalanceAmount = quote.getBidAmount() - quote.getAskAmount();
      imbalanceSide = OrderSide.ASK;
      imbalanceRatio = quote.getAskAmount() != 0 ? quote.getBidAmount() / quote.getAskAmount() : -1;
    }

    return new Imbalance(imbalanceSide, imbalanceAmount, imbalanceRatio);
  }

  /**
   * Creates liquidity at stressful time e.g. when no liquidity exist on a side or book is unbalanced above a threshold
   */
  private Order createLiquidityAtStress(Quote quote, Imbalance imbalance) {
    float price;
    float askCeiling = properties.getMaxPrice();
    float bidFloor = properties.getMinPrice();
    int riskDistance = properties.getRiskDistance();
    int imbalanceRounding = 10;

    if (imbalance.getSide() == OrderSide.ASK) {
      // ask imbalance
      val bestAsk = generateBestAsk(quote);
      price = bestAsk + (minQuoteSize * Math.round(imbalance.getRatio() / imbalanceRounding));
      price = price <= askCeiling ? price : askCeiling - (properties.getTickSize() * (randomInt(0, riskDistance) + 1));
    } else {
      // bid imbalance
      val bestBid = generateBestBid(quote);
      price = bestBid - (minQuoteSize * Math.round(imbalance.getRatio() / imbalanceRounding));
      price = price >= bidFloor ? price : bidFloor + (properties.getTickSize() * (randomInt(0, riskDistance) + 1));
    }
    price = Math.round(price * 100) / 100f;
    return new Order(imbalance.getSide(), OrderType.LIMIT_ADD, getSimulationTime(), Exchange.nextOrderId(), broker,
        quote.getSymbol(), decideAmount(), price, name);
  }

  private float generateBestBid(Quote quote) {
    // TODO: Explain why 10
    return quote.getBidAmount() != 0 ? quote.getBid() : properties.getMinPrice();
  }

  private float generateBestAsk(Quote quote) {
    // TODO: Explain why 12
    return quote.getAskAmount() != 0 ? quote.getAsk() : properties.getMaxPrice();
  }

  @Value
  private static class Imbalance {

    /**
     * The side which shows less depth e.g. if askSize = 10,000 and bidSize = 5,000. Then Bid is imbalanced.
     */
    OrderSide side;
    int amount;
    float ratio;

  }

}

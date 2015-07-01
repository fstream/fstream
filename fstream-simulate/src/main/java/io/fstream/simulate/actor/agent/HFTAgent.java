package io.fstream.simulate.actor.agent;

import static io.fstream.simulate.actor.agent.Agent.AgentType.HFT;
import io.fstream.simulate.actor.Exchange;
import io.fstream.simulate.message.ActiveInstruments;
import io.fstream.simulate.model.LimitOrder;
import io.fstream.simulate.model.Order.OrderSide;
import io.fstream.simulate.model.Order.OrderType;
import io.fstream.simulate.model.Quote;
import io.fstream.simulate.util.PrototypeActor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import akka.actor.ActorRef;

@Slf4j
@PrototypeActor
public class HFTAgent extends AgentActor {

  public HFTAgent(String name, ActorRef exchange) {
    super(HFT, name, exchange);
  }

  @Override
  public void executeAction() {
    // No-op since the agent is internally passive
  }

  @Override
  public void onReceive(Object message) throws Exception {
    if (message instanceof Quote) {
      onReceiveQuote(((Quote) message));
    } else if (message instanceof ActiveInstruments) {
      onReceiveActiveInstruments((ActiveInstruments) message);
    }
  }

  @Override
  protected void onReceiveQuote(Quote quote) {
    super.onReceiveQuote(quote);

    float spread = quote.getAskPrice() - quote.getBidPrice();
    val imbalance = getImbalance(quote);
    LimitOrder order;

    if (imbalance.getRatio() < 2 && spread > minTickSize) { // no stress period, so provide liquidity at better price
      order = createLiquidityNormal(quote, imbalance);
    } else {
      order = createLiquidityAtStress(quote, imbalance);
    }

    // Cancel any existing orders in the book
    cancelAllOpenOrders(order.getSymbol());

    // TODO: For now optimistically assume all LimitOrders sent are accepted by the exchange (no rejects)
    exchange.tell(order, self());
    openOrderBook.addOpenOrder(order);
  }

  private LimitOrder createLiquidityNormal(Quote quote, Imbalance imbalance) {
    float bestask = quote.getAskDepth() != 0 ? quote.getAskPrice() : 12;
    float bestbid = quote.getBidDepth() != 0 ? quote.getBidPrice() : 10;
    float price;

    if (imbalance.getSide() == OrderSide.ASK) {
      // ask imbalance
      price = Math.max(bestbid + minTickSize, bestask - minTickSize / 2);
      if ((price - bestbid) < minTickSize) {
        log.error("invalid spread/ask ask = {}, bid = {}, spread = {}. rejecting", price, bestbid, price - bestbid);
        price = bestask;
      }
    } else {
      // bid imbalance
      price = Math.min(bestask - minTickSize, bestbid + minTickSize / 2);
      if ((bestask - price) < minTickSize) {
        log.error("invalid spread/bid ask = {}, bid = {}, spread = {}. rejecting", bestask, price, bestask - price);
        price = bestask;
      }
    }

    return new LimitOrder(imbalance.getSide(), OrderType.ADD, getSimulationTime(), Exchange.nextOrderId(), broker,
        quote.getSymbol(),
        imbalance.getAmount(), price,
        name);
  }

  private Imbalance getImbalance(Quote quote) {
    int imbalanceAmount;
    OrderSide imbalanceSide;
    float imbalanceRatio;

    if (quote.getAskDepth() > quote.getBidDepth()) {
      imbalanceAmount = quote.getAskDepth() - quote.getBidDepth();
      imbalanceSide = OrderSide.BID;
      imbalanceRatio = quote.getBidDepth() != 0 ? quote.getAskDepth() / quote.getBidDepth() : 0;
    } else {
      // TODO: If both sides are 0, then this will bias creation of ASK liquidity. Not a big deal now, but fix later.
      imbalanceAmount = quote.getBidDepth() - quote.getAskDepth();
      imbalanceSide = OrderSide.ASK;
      imbalanceRatio = quote.getAskDepth() != 0 ? quote.getBidDepth() / quote.getAskDepth() : 0;
    }

    return new Imbalance(imbalanceSide, imbalanceAmount, imbalanceRatio);
  }

  /**
   * Creates liquidity at stressful time e.g. when no liquidity exist on a side or book is unbalanced above a threshold
   */
  private LimitOrder createLiquidityAtStress(Quote quote, Imbalance imbalance) {
    // TODO: 12 and 10?
    val bestAsk = quote.getAskDepth() != 0 ? quote.getAskPrice() : 12;
    val bestBid = quote.getBidDepth() != 0 ? quote.getBidPrice() : 10;
    float price;

    if (imbalance.getSide() == OrderSide.ASK) {
      // ask imbalance
      price = bestAsk + (getMinTickSize() * imbalance.getRatio());
    } else {
      // bid imbalance
      price = bestBid - (getMinTickSize() * imbalance.getRatio());
    }

    return new LimitOrder(imbalance.getSide(), OrderType.ADD, getSimulationTime(), Exchange.nextOrderId(), broker,
        quote.getSymbol(), imbalance.getAmount(), price, name);
  }

  @Data
  @AllArgsConstructor
  private class Imbalance {

    /**
     * The side which shows less depth e.g. if askSize = 10,000 and bidSize = 5,000. Then Bid is imbalanced.
     */
    OrderSide side;
    int amount;
    float ratio;

  }

}

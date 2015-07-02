package io.fstream.simulate.actor.agent;

import static io.fstream.simulate.actor.agent.AgentType.HFT;
import io.fstream.simulate.actor.Exchange;
import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.message.ActiveInstruments;
import io.fstream.simulate.model.LimitOrder;
import io.fstream.simulate.model.Order.OrderSide;
import io.fstream.simulate.model.Order.OrderType;
import io.fstream.simulate.model.Quote;
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
  public void onReceive(Object message) throws Exception {
    if (message instanceof Quote) {
      onReceiveQuote((Quote) message);
    } else if (message instanceof ActiveInstruments) {
      onReceiveActiveInstruments((ActiveInstruments) message);
    }
  }

  @Override
  protected void onReceiveQuote(Quote quote) {
    super.onReceiveQuote(quote);

    val imbalance = getImbalance(quote);
    val order = isNormal(quote, imbalance) ?
        createLiquidityNormal(quote, imbalance) :
        createLiquidityAtStress(quote, imbalance);

    // Cancel any existing orders in the book
    cancelAllOpenOrders(order.getSymbol());

    // TODO: For now optimistically assume all LimitOrders sent are accepted by the exchange (no rejects)
    exchange().tell(order, self());
    openOrders.addOpenOrder(order);
  }

  private boolean isNormal(Quote quote, Imbalance imbalance) {
    val spread = quote.getAskPrice() - quote.getBidPrice();

    return imbalance.getRatio() < 2 && spread > minTickSize;
  }

  private LimitOrder createLiquidityNormal(Quote quote, Imbalance imbalance) {
    // No stress period, so provide liquidity at better price
    val bestAsk = generateBestAsk(quote);
    val bestBid = generateBestBid(quote);
    float price;

    if (imbalance.getSide() == OrderSide.ASK) {
      // Ask imbalance
      price = Math.max(bestBid + minTickSize, bestAsk - minTickSize / 2);
      if ((price - bestBid) < minTickSize) {
        log.error("Invalid spread/ask ask = {}, bid = {}, spread = {}. rejecting", price, bestBid, price - bestBid);
        price = bestAsk;
      }
    } else {
      // Bid imbalance
      price = Math.min(bestAsk - minTickSize, bestBid + minTickSize / 2);
      if ((bestAsk - price) < minTickSize) {
        log.error("Invalid spread/bid ask = {}, bid = {}, spread = {}. rejecting", bestAsk, price, bestAsk - price);
        price = bestAsk;
      }
    }

    return new LimitOrder(imbalance.getSide(), OrderType.ADD, getSimulationTime(), Exchange.nextOrderId(), broker,
        quote.getSymbol(), imbalance.getAmount(), price, name);
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
    float price;

    if (imbalance.getSide() == OrderSide.ASK) {
      // ask imbalance
      val bestAsk = generateBestAsk(quote);
      price = bestAsk + (minTickSize * imbalance.getRatio());
    } else {
      // bid imbalance
      val bestBid = generateBestBid(quote);
      price = bestBid - (minTickSize * imbalance.getRatio());
    }

    return new LimitOrder(imbalance.getSide(), OrderType.ADD, getSimulationTime(), Exchange.nextOrderId(), broker,
        quote.getSymbol(), imbalance.getAmount(), price, name);
  }

  private float generateBestBid(Quote quote) {
    // TODO: 10?
    return quote.getBidDepth() != 0 ? quote.getBidPrice() : 10;
  }

  private float generateBestAsk(Quote quote) {
    // TODO: 12?
    return quote.getAskDepth() != 0 ? quote.getAskPrice() : 12;
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

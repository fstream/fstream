package io.fstream.simulate.actor.agent;

import io.fstream.simulate.actor.Exchange;
import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.message.ActiveInstruments;
import io.fstream.simulate.message.SubscriptionQuote;
import io.fstream.simulate.model.LimitOrder;
import io.fstream.simulate.model.Order.OrderSide;
import io.fstream.simulate.model.Order.OrderType;
import io.fstream.simulate.model.Quote;
import io.fstream.simulate.util.PrototypeActor;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;

import akka.actor.ActorRef;

@Profile("hft")
@PrototypeActor
@Slf4j
public class HFTAgent extends AgentActor {

  @Autowired
  private SimulateProperties properties;
  float probBuy;

  public HFTAgent(String name, ActorRef exchange) {
    super(name, exchange);
    quoteSubscriptionLevel = properties.getHft().getQuoteSubscriptionLevel();
    probBuy = properties.getHft().getProbBuy();
    activeinstruments = new ActiveInstruments();
  }

  // TODO don't really need this. Action is always Quote driven
  ActiveInstruments activeinstruments;

  @Override
  public void executeAction() {
    // No-op
  }

  @Override
  public void onReceive(Object message) throws Exception {
    if (message instanceof Quote) {
      this.getBbboQuotes().put(((Quote) message).getSymbol(), (Quote) message);
      executeAction();
    }
    else if (message instanceof ActiveInstruments) {
      this.activeinstruments.setInstruments(((ActiveInstruments) message).getInstruments());
    }
  }

  private void decideActionOnQuote(Quote quote) {
    float spread = quote.getAskprice() - quote.getBidprice();
    Imbalance imbalance = getImbalance(quote);
    if (imbalance.getRatio() < 2 && spread > minTickSize) { // no stress period, so provide liquidity at better price
      @NonNull
      LimitOrder order = createLiquidityNormal(quote, imbalance);
      exchange.tell(order, self());
    }
    else {

    }

  }

  private LimitOrder createLiquidityNormal(Quote quote, Imbalance imbalance) {
    float bestask = quote.getAskdepth() != 0 ? quote.getAskprice() : 12;
    float bestbid = quote.getBiddepth() != 0 ? quote.getBidprice() : 10;
    float price;
    if (imbalance.getSide() == OrderSide.ASK) { // ask imbalance
      price = bestask - minTickSize / 2;
      if ((price - bestbid) < minTickSize) {
        log.error("invalid spread/price ask = {}, bid = {}, spread = {}. rejecting", price, bestbid, price - bestbid);
        price = bestask;
      }
    }
    else { // bid imbalance
      price = bestbid + minTickSize / 2;
      if ((bestask - price) > minTickSize) {
        log.error("invalid spread/price ask = {}, bid = {}, spread = {}. rejecting", bestask, price, bestask - price);
        price = bestask;
      }
    }
    return new LimitOrder(imbalance.getSide(), OrderType.ADD, DateTime.now(), Exchange.nextOrderId(), "yy",
        quote.getSymbol(),
        imbalance.getAmount(), price,
        name);

  }

  private Imbalance getImbalance(Quote quote) {

    int imbalanceAmount;
    OrderSide imbalanceSide;
    float imbalanceRatio;
    if (quote.getAskdepth() > quote.getBiddepth()) {
      imbalanceAmount = quote.getAskdepth() - quote.getBiddepth();
      imbalanceSide = OrderSide.BID;
      imbalanceRatio = quote.getBidprice() != 0 ? quote.getAskdepth() / quote.getBiddepth() : 0;
    }
    // TODO if both sides are 0, then this will bias creation of ASK liquidity. Not a big deal now, but fix later.
    else {
      imbalanceAmount = quote.getBiddepth() - quote.getAskdepth();
      imbalanceSide = OrderSide.ASK;
      imbalanceRatio = quote.getAskprice() != 0 ? quote.getBiddepth() / quote.getAskdepth() : 0;
    }
    return new Imbalance(imbalanceSide, imbalanceAmount, imbalanceRatio);
  }

  /*
   * creates liqduity at stressfull time e.g. when no liquidity exist on a side or book is unbalanced above a threshold
   */
  private LimitOrder createLiquidityAtStress(Quote quote, Imbalance imbalance) {
    float bestask = quote.getAskdepth() != 0 ? quote.getAskprice() : 12;
    float bestbid = quote.getBiddepth() != 0 ? quote.getBidprice() : 10;
    float price;
    if (imbalance.getSide() == OrderSide.ASK) { // ask imbalance
      price = bestask + (getMinTickSize() * imbalance.getRatio());
    }
    else { // bid imbalance
      price = bestbid - (getMinTickSize() * imbalance.getRatio());
      // if ((bestask - price) > minTickSize) {
      // log.error("invalid spread/price ask = {}, bid = {}, spread = {}. rejecting", bestask, price, bestask - price);
      // price = bestask;
      // }
    }
    return new LimitOrder(imbalance.getSide(), OrderType.ADD, DateTime.now(), Exchange.nextOrderId(), "yy",
        quote.getSymbol(),
        imbalance.getAmount(), price,
        name);
  }

  @Override
  public void preStart() {
    // Register to receive quotes
    exchange.tell(new SubscriptionQuote(this.getQuoteSubscriptionLevel()), self());
  }

  @Data
  private class Imbalance {

    /**
     * The side which shows less depth e.g. if askSize = 10,000 and bidSize = 5,000. Then Bid is imbalanced.
     */
    OrderSide side;
    int amount;
    float ratio;

    public Imbalance(OrderSide side, int amount, float ratio) {
      this.side = side;
      this.amount = amount;
      this.ratio = ratio;
    }
  }
}

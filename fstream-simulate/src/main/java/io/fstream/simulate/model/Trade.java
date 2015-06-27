package io.fstream.simulate.model;

import io.fstream.simulate.model.Order.OrderSide;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.joda.time.DateTime;

@Data
@EqualsAndHashCode
public class Trade {

  public enum TradeSide {
    ACTIVE, PASSIVE
  };

  private String buyUser;
  private String sellUser;
  private float price;
  private boolean activeBuy;
  private String symbol;
  private DateTime time;
  private DateTime orderTime;
  private int amount;

  // TODO ordertime needs to be initialized
  public Trade(DateTime tradeTime, Order active, Order passive, int executedSize) {
    this.setPrice(passive.getPrice());
    this.setSymbol(active.getSymbol());
    this.setTime(tradeTime);

    // use active orders timestamp as tradetime. Simplifying assumption
    this.setAmount(executedSize);
    if (active.getSide() == OrderSide.ASK) {
      // active seller
      this.sellUser = active.getUserId();
      this.activeBuy = false;
      this.buyUser = passive.getUserId();
    } else {
      // active buy
      this.sellUser = passive.getUserId();
      this.activeBuy = true;
      this.buyUser = active.getUserId();
    }
  }

}

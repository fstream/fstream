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

  public Trade(DateTime tradetime, Order active, Order passive, int executedSize) {
    this.setPrice(passive.getPrice());
    this.setSymbol(active.getSymbol());
    this.setTime(tradetime);
    this.setOrderTime(active.getSentTime());
    this.setAmount(executedSize);

    // Use active orders timestamp as trade time as a simplifying assumption
    if (active.getSide() == OrderSide.ASK) {
      // Active seller
      this.sellUser = active.getUserId();
      this.activeBuy = false;
      this.buyUser = passive.getUserId();
    } else {
      // Active buy
      this.sellUser = passive.getUserId();
      this.activeBuy = true;
      this.buyUser = active.getUserId();
    }
  }

}

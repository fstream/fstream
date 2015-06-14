package io.fstream.simulate.orders;

import io.fstream.simulate.orders.Order.OrderSide;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.joda.time.DateTime;

@Data
@EqualsAndHashCode
public class Trade {

  public enum TradeSide {
    ACTIVE, PASSIVE
  };

  private String buyuser;
  private String selluser;
  private float price;
  private boolean activebuy;
  private String symbol;
  private DateTime time;
  private DateTime ordertime;
  private int amount;

  public Trade(String buyuser, String selluser, boolean activebuy, float price, String symbol, DateTime datetime,
      int amount) {
    this.buyuser = buyuser;
    this.selluser = selluser;
    this.price = price;
    this.activebuy = activebuy;
    this.time = datetime;
    this.amount = amount;
  }

  public Trade(DateTime tradetime, Order active, Order passive, int executedsize) {
    this.setPrice(passive.getPrice());
    this.setSymbol(symbol);
    this.setTime(tradetime);

    // use active orders timestamp as tradetime. Simplifying assumption
    this.setAmount(executedsize);
    if (active.getSide() == OrderSide.ASK) {
      // active seller
      this.selluser = active.getUserId();
      this.activebuy = false;
      this.buyuser = passive.getUserId();
    } else {
      // active buy
      this.selluser = passive.getUserId();
      this.activebuy = true;
      this.buyuser = active.getUserId();
    }
  }

  // @Override
  // public String toString() {
  // return String.format("%s,%s,%s,%s,%s", this.getBuyuser(), this.getSelluser(), this.isActivebuy(), this.getAmount(),
  // this.getPrice());
  //
  // }

}

package io.fstream.simulate.model;

import lombok.Data;
import lombok.val;

import org.joda.time.DateTime;

@Data
public class LimitOrder implements Order, Comparable<LimitOrder> {

  private OrderSide side;
  private OrderType type;
  private DateTime time;
  private int oid;
  private String brokerId;
  private String symbol;
  private int amount;
  private float price;
  private String userId;

  private DateTime processedTime;

  public LimitOrder(OrderSide side, OrderType type, DateTime time, int oid, String brokerId, String symbol, int amount,
      float price, String userId) {
    this.side = side;
    this.type = type;
    this.time = time;
    this.oid = oid;
    this.brokerId = brokerId;
    this.symbol = symbol;
    this.amount = amount;
    this.price = price;
    this.userId = userId;
  }

  @Override
  public DateTime getSentTime() {
    return time;
  }

  @Override
  public boolean equals(Object obj) {
    val order = (LimitOrder) obj;
    if (order.getBrokerId() == this.brokerId && order.getOid() == this.oid
        && order.getSentTime().equals(this.getSentTime())) {
      return true;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return this.getBrokerId().hashCode() + this.getOid() + this.getSentTime().hashCode();
  }

  @Override
  public int compareTo(LimitOrder o) {
    if (this.getOid() == o.getOid() && this.brokerId == o.getBrokerId()) {
      return 1;
    }

    return 0;
  }

}

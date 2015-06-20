package io.fstream.simulate.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.val;

import org.joda.time.DateTime;

@Getter
@Setter
@ToString
public class LimitOrder implements Order, Comparable<LimitOrder> {

  OrderSide side;
  OrderType type;
  DateTime time;
  int oid;
  String brokerId;
  String symbol;
  int amount;
  float price;
  String userId;
  DateTime processedTime;

  public LimitOrder(OrderSide side, OrderType type, DateTime time, int oid, String brokerId, String symbol, int amount,
      float price, String userid) {
    this.side = side;
    this.type = type;
    this.time = time;
    this.oid = oid;
    this.brokerId = brokerId;
    this.symbol = symbol;
    this.amount = amount;
    this.price = price;
    this.userId = userid;
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

  @Override
  public String getUserId() {
    return this.userId;
  }

  @Override
  public DateTime getSentTime() {
    return time;
  }

}

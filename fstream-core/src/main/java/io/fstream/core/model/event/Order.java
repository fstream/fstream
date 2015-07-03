package io.fstream.core.model.event;

import static io.fstream.core.model.event.EventType.ORDER;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.val;

import org.joda.time.DateTime;

@Getter
@Setter
@ToString
public class Order extends AbstractEvent implements Comparable<Order> {

  public enum OrderSide {
    BID,
    ASK // Offer
  };

  public enum OrderType {
    MO, // Market Order
    ADD, // Limit Order - Add
    AMEND, // Limit Order - Amend
    CANCEL // Limit Order - Cancel
  }

  private OrderSide side;
  private OrderType orderType;
  private int oid;
  private String brokerId;
  private String symbol;
  private int amount;
  private float price;
  private String userId;

  private DateTime processedTime;

  public Order(@NonNull OrderSide side, @NonNull OrderType type, @NonNull DateTime time, int oid,
      @NonNull String brokerId, @NonNull String symbol, int amount, float price, @NonNull String userId) {
    super(time);
    this.side = side;
    this.orderType = type;
    this.oid = oid;
    this.brokerId = brokerId;
    this.symbol = symbol;
    this.amount = amount;
    this.price = price;
    this.userId = userId;
  }

  @Override
  public boolean equals(Object obj) {
    val order = (Order) obj;
    if (order.getBrokerId() == this.brokerId && order.getOid() == this.oid
        && order.getDateTime().equals(this.getDateTime())) {
      return true;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return this.getBrokerId().hashCode() + this.getOid() + this.getDateTime().hashCode();
  }

  @Override
  public int compareTo(Order o) {
    if (this.getOid() == o.getOid() && this.brokerId == o.getBrokerId()) {
      return 1;
    }

    return 0;
  }

  @Override
  public EventType getType() {
    return ORDER;
  }

}

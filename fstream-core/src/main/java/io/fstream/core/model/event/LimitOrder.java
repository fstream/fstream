package io.fstream.core.model.event;

import static io.fstream.core.model.event.EventType.ORDER;
import lombok.Data;
import lombok.NonNull;
import lombok.val;

import org.joda.time.DateTime;

@Data
public class LimitOrder extends AbstractEvent implements Order, Comparable<LimitOrder> {

  private OrderSide side;
  private OrderType orderType;
  private int oid;
  private String brokerId;
  private String symbol;
  private int amount;
  private float price;
  private String userId;

  private DateTime processedTime;

  public LimitOrder(@NonNull OrderSide side, @NonNull OrderType type, @NonNull DateTime time, int oid,
      @NonNull String brokerId,
      @NonNull String symbol,
      int amount,
      float price, @NonNull String userId) {
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
    val order = (LimitOrder) obj;
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
  public int compareTo(LimitOrder o) {
    if (this.getOid() == o.getOid() && this.brokerId == o.getBrokerId()) {
      return 1;
    }

    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.fstream.core.model.event.Event#getType()
   */
  @Override
  public EventType getType() {
    return ORDER;
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.fstream.core.model.event.Order#getOrderType()
   */
  @Override
  public OrderType getOrderType() {
    return orderType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.fstream.core.model.event.Order#setType(io.fstream.core.model.event.Order.OrderType)
   */
  @Override
  public void setOrderType(OrderType type) {
    this.orderType = type;

  }

}

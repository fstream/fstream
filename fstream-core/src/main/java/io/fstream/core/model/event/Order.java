package io.fstream.core.model.event;

import static io.fstream.core.model.event.EventType.ORDER;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

import org.joda.time.DateTime;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false, of = { "brokerId", "oid" })
public class Order extends AbstractEvent {

  public enum OrderSide {
    BID,
    ASK // Offer
  };

  public enum OrderType {
    MARKET_ORDER,
    LIMIT_ADD,
    LIMIT_AMEND,
    LIMIT_CANCEL
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
  public EventType getType() {
    return ORDER;
  }

}

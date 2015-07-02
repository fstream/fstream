package io.fstream.core.model.event;

import static io.fstream.core.model.event.EventType.TRADE;
import io.fstream.core.model.event.Order.OrderSide;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.joda.time.DateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TradeEvent extends AbstractEvent {

  public enum TradeSide {
    ACTIVE, PASSIVE
  };

  private String buyUser;
  private String sellUser;
  private float price;
  private boolean activeBuy;
  private String symbol;
  private DateTime orderTime;
  private int amount;

  public TradeEvent(DateTime tradeTime, Order active, Order passive, int executedSize) {
    super(tradeTime);
    this.setPrice(passive.getPrice());
    this.setSymbol(active.getSymbol());
    this.setOrderTime(active.getDateTime());
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

  @Override
  public EventType getType() {
    return TRADE;
  }

}

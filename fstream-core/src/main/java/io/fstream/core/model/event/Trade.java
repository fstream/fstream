package io.fstream.core.model.event;

import static io.fstream.core.model.event.EventType.TRADE;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.joda.time.DateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Trade extends AbstractEvent {

  public enum TradeSide {
    ACTIVE, PASSIVE
  };

  private String buyUser;
  private String sellUser;
  private float price;
  private boolean activeBuy;
  private String symbol;
  private int amount;

  public Trade(DateTime dateTime, String buyUser, String sellUser, float price, boolean activeBuy, String symbol,
      int amount) {
    this.dateTime = dateTime;
    this.buyUser = buyUser;
    this.sellUser = sellUser;
    this.price = price;
    this.activeBuy = activeBuy;
    this.symbol = symbol;
    this.amount = amount;
  }

  @Override
  public EventType getType() {
    return TRADE;
  }

}

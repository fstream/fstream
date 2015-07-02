package io.fstream.simulate.model;

import org.joda.time.DateTime;

// TODO: Bring back MarketOrder. LimitOrder (has price) extend MarketOrder?
public interface Order {

  enum OrderSide {
    BID,
    ASK // Offer
  };

  enum OrderType {
    MO, // Market Order
    ADD, // Limit Order - Add
    AMEND, // Limit Order - Amend
    CANCEL // Limit Order - Cancel
  }

  OrderType getType();

  OrderSide getSide();

  DateTime getSentTime();

  int getOid();

  String getBrokerId();

  int getAmount();

  String getSymbol();

  float getPrice();

  String getUserId();

  void setProcessedTime(DateTime datetime);

  void setType(OrderType type);

}

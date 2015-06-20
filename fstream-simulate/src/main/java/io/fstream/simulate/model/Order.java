package io.fstream.simulate.model;

import org.joda.time.DateTime;

// TODO: bring back MarketOrder. LimitOrder (has price) extend MarketOrder ?
public interface Order {

  enum OrderSide {
    BID, ASK
  }; // ASK(OFFER)/BID

  enum OrderType {
    MO, ADD, AMEND, CANCEL
  } // Market Order, Limit Order (add, amend, cancel)

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

}

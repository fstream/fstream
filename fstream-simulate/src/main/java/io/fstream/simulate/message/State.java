package io.fstream.simulate.message;

import io.fstream.simulate.orders.LimitOrder;

import java.util.TreeMap;
import java.util.TreeSet;

import lombok.Getter;
import lombok.Setter;

import org.joda.time.DateTime;

@Getter
@Setter
public class State {

  TreeMap<Float, TreeSet<LimitOrder>> bids;
  TreeMap<Float, TreeSet<LimitOrder>> asks;
  String symbol;

  float bestbid;
  float bestask;

  int biddepth;
  int askdepth;

  DateTime time;

  int status = 0; // 0 request, 1 response

  public State(String symbol) {
    this.symbol = symbol;
  }

}

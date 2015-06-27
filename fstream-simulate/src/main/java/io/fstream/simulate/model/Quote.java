package io.fstream.simulate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.joda.time.DateTime;

@Data
@AllArgsConstructor
public class Quote {

  DateTime time;
  String symbol;

  float askPrice;
  float bidPrice;

  int askDepth;
  int bidDepth;

}

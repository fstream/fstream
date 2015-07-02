package io.fstream.simulate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.joda.time.DateTime;

@Data
@AllArgsConstructor
public class Quote {

  private DateTime time;
  private String symbol;

  private float askPrice;
  private float bidPrice;

  private int askDepth;
  private int bidDepth;

}

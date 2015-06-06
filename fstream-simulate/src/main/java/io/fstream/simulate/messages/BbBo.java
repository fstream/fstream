package io.fstream.simulate.messages;

import lombok.Getter;
import lombok.Setter;

/**
 * Data structure to represent best bid/best offer
 * 
 * @author bdevani
 *
 *
 */

@Getter
@Setter
public class BbBo {

  String symbol;
  float bestbid;
  float bestoffer;

  public BbBo(String symbol) {
    this.symbol = symbol;
  }

}

/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.util;

import static io.fstream.core.util.Formatters.formatCount;
import lombok.Getter;

@Getter
public class EventStats {

  private int tradeCount;
  private int orderCount;
  private int quoteCount;
  private int snapshotCount;

  public void incrementTradeCount() {
    tradeCount++;
  }

  public void incrementOrderCount() {
    orderCount++;
  }

  public void incrementQuoteCount() {
    quoteCount++;
  }

  public void incrementSnapshotCount() {
    snapshotCount++;
  }

  @Override
  public String toString() {
    return String.format("orders[%9s] trades[%9s] quotes[%3s] snapshots[%3s]",
        formatCount(orderCount), formatCount(tradeCount), formatCount(quoteCount), formatCount(snapshotCount));
  }

}
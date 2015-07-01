/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.util;

import io.fstream.simulate.model.LimitOrder;

import java.util.Comparator;

/**
 * Sorts limit orders in time priority in the order book TreeSet
 * <p>
 * (Price -> {ordert1, ordert2, ...}
 */
public final class LimitOrderTimeComparator implements Comparator<LimitOrder> {

  public static final LimitOrderTimeComparator INSTANCE = new LimitOrderTimeComparator();

  @Override
  public int compare(LimitOrder order1, LimitOrder order2) {
    if (order1.equals(order2)) {
      return 0;
    }
    if (order1.getSentTime().getMillis() < order2.getSentTime().getMillis()) {
      return -1;
    } else if (order1.getSentTime().getMillis() > order2.getSentTime().getMillis()) {
      return 1;
    } else {
      // TODO: This is a hack so orders at same time are not ignored. need a better data structure for orders
      return -1;
    }
  }

}
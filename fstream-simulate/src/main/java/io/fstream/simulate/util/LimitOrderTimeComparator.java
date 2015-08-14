/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.util;

import io.fstream.core.model.event.Order;

import java.util.Comparator;

/**
 * Sorts limit orders in time priority in the order book TreeSet
 * <p>
 * (Price -> {ordert1, ordert2, ...}
 */
public final class LimitOrderTimeComparator implements Comparator<Order> {

  public static final LimitOrderTimeComparator INSTANCE = new LimitOrderTimeComparator();

  @Override
  public int compare(Order order1, Order order2) {

    if (order1.equals(order2)) {
      return 0;
    }

    long diff = order1.getDateTime().getMillis() - order2.getDateTime().getMillis();
    if (diff > 0) {
      return 1;
    }
    else if (diff < 0) {
      return -1;
    }
    else {
      return 1;
    }
  }

}
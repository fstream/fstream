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
import io.fstream.core.model.event.Order.OrderType;

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
    // TODO this is needed for remove from treemap to happen properly. Otherwise it will traverse the tree
    if (order1.equals(order2) || (order1.getOrderType() == OrderType.LIMIT_CANCEL)) {
      return 0;
    }

    if (order1.getDateTime().getMillis() < order2.getDateTime().getMillis()) {
      return -1;
    } else if (order1.getDateTime().getMillis() > order2.getDateTime().getMillis()) {
      return 1;
    } else {
      // TODO: This is a hack so orders at same time are not ignored. need a better data structure for orders
      return -1;
    }
  }

}
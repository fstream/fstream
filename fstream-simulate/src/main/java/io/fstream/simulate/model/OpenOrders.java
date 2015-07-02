/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.model;

import io.fstream.core.model.event.Order;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

@Slf4j
@Value
public class OpenOrders {

  private final Multimap<String, Order> orders = HashMultimap.create();

  public boolean addOpenOrder(@NonNull Order order) {
    if (orders.get(order.getSymbol()).contains(order)) {
      log.warn("Order already present in open orders {}", order);
      return false;
    }

    return orders.put(order.getSymbol(), order);
  }

}

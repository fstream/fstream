/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * 
 */
@Component
@Scope("prototype")
@NoArgsConstructor
@Slf4j
@Getter
public class OpenOrders {

  final Multimap<String, Order> orders = HashMultimap.create();

  public boolean addOpenOrder(Order order) {
    if (orders.get(order.getSymbol()).contains(order)) {
      log.warn("order already present in open orders {}", order.toString());
      return false;
    }
    return orders.put(order.getSymbol(), order);
  }

}

/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.model;

import static io.fstream.core.model.event.Order.OrderSide.ASK;
import static java.util.Collections.reverseOrder;
import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Order.OrderSide;
import io.fstream.simulate.util.LimitOrderTimeComparator;

import java.util.Iterator;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

/**
 * The main data structure for representing one half of an order book.
 */
public class BookSide {

  /**
   * The side of the order book
   */
  private final OrderSide side;

  /**
   * Ordered price levels.
   */
  private final NavigableMap<Float, NavigableSet<Order>> priceLevels;

  /**
   * The depth of the book side.
   */
  @Getter
  private int depth;

  public BookSide(@NonNull OrderSide side) {
    this.side = side;
    this.priceLevels = side == ASK ? new TreeMap<>() : new TreeMap<>(reverseOrder());
  }

  /**
   * Return an iterator in price order.
   * <p>
   * Price is ascending for bids and descending for asks.
   */
  public Iterator<NavigableSet<Order>> iterator() {
    return priceLevels.values().iterator();
  }

  public boolean isEmpty() {
    return priceLevels.isEmpty();
  }

  public int getPriceLevelCount() {
    return priceLevels.size();
  }

  public void addOrder(@NonNull Order order) {
    val price = order.getPrice();
    NavigableSet<Order> priceLevel = priceLevels.get(price);
    if (priceLevel == null) {
      priceLevel = new TreeSet<Order>(LimitOrderTimeComparator.INSTANCE);

      priceLevels.put(price, priceLevel);
    }

    priceLevel.add(order);
    depth += order.getAmount();
  }

  public boolean removeOrder(@NonNull Order order) {
    val price = order.getPrice();
    val priceLevel = getPriceLevel(price);

    val empty = priceLevel == null;
    if (empty) {
      return false;
    }

    val missing = !priceLevel.remove(order);
    if (missing) {
      return false;
    }

    if (priceLevel.isEmpty()) {
      // Prune empty levels to prevent memory accumulation
      priceLevels.remove(price);
    }

    depth -= order.getAmount();
    return true;
  }

  public NavigableSet<Order> getPriceLevel(float price) {
    return priceLevels.get(price);
  }

  public float getBestPrice() {
    if (isEmpty()) {
      return side == ASK ? Float.MAX_VALUE : Float.MIN_VALUE;
    }

    return priceLevels.firstKey();
  }

  public int calculateDepth() {
    int depth = 0;
    for (val priceLevel : priceLevels.values()) {
      for (val order : priceLevel) {
        depth += order.getAmount();
      }
    }

    return depth;
  }

  public int calculatePriceLevelDepth(float price) {
    val priceLevel = getPriceLevel(price);
    if (priceLevel == null) {
      return 0;
    }

    int depth = 0;
    for (val order : priceLevel) {
      depth += order.getAmount();
    }

    return depth;
  }

  public int calculateOrderCount() {
    int count = 0;
    for (val priceLevel : priceLevels.values()) {
      count += priceLevel.size();
    }

    return count;
  }

  @Override
  public String toString() {
    String text = "";
    for (val bid : priceLevels.entrySet()) {
      text += String.format("%s -> ", bid.getKey());
      for (val firstOrder : bid.getValue()) {
        text += String.format("( %s,%s,%s) -> ",
            firstOrder.getDateTime().toString(), firstOrder.getPrice(), firstOrder.getAmount());
      }
      text = text + "\n";
    }

    return text;
  }

}
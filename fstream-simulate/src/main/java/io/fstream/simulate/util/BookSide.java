/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.util;

import static io.fstream.core.model.event.Order.OrderSide.ASK;
import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Order.OrderSide;

import java.util.Iterator;
import java.util.NavigableSet;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

/**
 * The main data structure for representing one half of an order book.
 */
@Slf4j
public class BookSide {

  /**
   * The side of the order book
   */
  private final OrderSide side;

  /**
   * Ordered price levels.
   */
  private final TreeMultimap<Float, Order> priceLevels;

  /**
   * The depth of the book side.
   */
  @Getter
  private int depth;

  public BookSide(@NonNull OrderSide side) {
    this.side = side;
    this.priceLevels = createPriceLevels(side);
  }

  /**
   * Return an iterator in (price, time) order.
   * <p>
   * Price is ascending for bids and descending for asks.
   */
  public Iterator<Order> iterator() {
    return priceLevels.values().iterator();
  }

  public int getPriceLevelCount() {
    return priceLevels.keySet().size();
  }

  public void addOrder(@NonNull Order order) {
    val price = order.getPrice();
    priceLevels.put(price, order);

    depth += order.getAmount();
  }

  public boolean removeOrder(@NonNull Order order) {
    val price = order.getPrice();

    val missing = !priceLevels.remove(price, order);
    if (missing) {
      return false;
    }

    depth -= order.getAmount();

    return true;
  }

  // TODO: Remove the need for this by forcing all consumers to repost on modification
  public void removeDepth(float amount) {
    depth -= amount;
  }

  public NavigableSet<Order> getPriceLevel(float price) {
    return priceLevels.get(price);
  }

  public float getBestPrice() {
    if (priceLevels.isEmpty()) {
      return side == ASK ? Float.MAX_VALUE : Float.MIN_VALUE;
    }

    return priceLevels.keySet().first();
  }

  public int calculateDepth() {
    int depth = 0;
    for (val order : priceLevels.values()) {
      depth += order.getAmount();
    }

    return depth;
  }

  public int calculatePriceDepth(float price) {
    int depth = 0;
    for (val order : getPriceLevel(price)) {
      depth += order.getAmount();
    }

    return depth;
  }

  public int calculateOrderCount() {
    return priceLevels.size();
  }

  public boolean isDepthValid() {
    val actual = calculateDepth();
    int expected = getDepth();

    val invalid = actual != expected;
    if (invalid) {
      log.error("Bid depth does not add up record = {} actual = {}", expected, actual);
    }

    return invalid;
  }

  @Override
  public String toString() {
    String text = "";
    for (val price : priceLevels.keySet()) {
      text += String.format("%s -> ", price);
      for (val order : priceLevels.get(price)) {
        text += String.format("( %s,%s,%s) -> ",
            order.getDateTime().toString(), order.getPrice(), order.getAmount());
      }

      text = text + "\n";
    }

    return text;
  }

  private TreeMultimap<Float, Order> createPriceLevels(OrderSide side) {
    val keyOrder = side == ASK ? Ordering.<Float> natural() : Ordering.<Float> natural().reverse();
    val valueOrder = LimitOrderTimeComparator.INSTANCE;

    return TreeMultimap.create(keyOrder, valueOrder);
  }

}
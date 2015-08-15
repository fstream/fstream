/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.util;

import static com.google.common.base.Preconditions.checkState;
import static io.fstream.core.model.event.Order.OrderSide.ASK;
import static io.fstream.core.util.Formatters.formatCount;
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
  private long depth;

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
    val delegate = priceLevels.values().iterator();
    return new Iterator<Order>() {

      private Order current;

      @Override
      public boolean hasNext() {
        return delegate.hasNext();
      }

      @Override
      public Order next() {
        current = delegate.next();
        return current;
      }

      @Override
      public void remove() {
        checkState(current != null);

        // Remove depth to ensure consistency
        removeDepth(current.getAmount());
        delegate.remove();

        current = null;
      }

    };
  }

  public Iterable<Float> getPrices() {
    return priceLevels.keySet();
  }

  public int getPriceLevelCount() {
    return priceLevels.keySet().size();
  }

  public void addOrder(@NonNull Order order) {
    val price = order.getPrice();
    // checkState(price != Float.MAX_VALUE && price != Float.MIN_VALUE, "Price not set for order! %s", order);

    priceLevels.put(price, order);

    depth += order.getAmount();
  }

  public boolean removeOrder(@NonNull Order order) {
    val price = order.getPrice();

    val orders = priceLevels.get(price);
    // since order (cancel_limit) request is sent my agent for an order that might
    // be partially filled on the orderbook. The amount to be removed is for what is on the book and not what is sent by
    // agent as cancel order
    int removedOrderAmount = 0;
    val ordersIterator = orders.iterator();
    boolean missing = true;
    while (ordersIterator.hasNext()) {
      val currentOrder = ordersIterator.next();
      if (currentOrder.equals(order)) {
        removedOrderAmount = currentOrder.getAmount();
        ordersIterator.remove(); // remove the order. This also avoids the issue that priceLevel.remove(price,order)
                                 // uses the comparator and that can cause issue when cancel order time != original
                                 // order time. see unit test
        missing = false;
        break;
      }
    }
    // val missing = !priceLevels.remove(price, order);
    if (missing) {
      return false;
    }

    depth -= removedOrderAmount;

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
      return 0;
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
    val expected = getDepth();

    val mismatch = actual != expected;
    if (mismatch) {
      log.error("{} depth does not match. expected = {} actual = {}, delta = {}", side, expected, actual, expected
          - actual);
      return false;
    }

    val negative = actual < 0;
    if (negative) {
      log.error("{} depth is negative: {}", side, actual);
      return false;
    }

    return true;
  }

  @Override
  public String toString() {
    String text = "";
    for (val price : priceLevels.keySet()) {
      text += String.format("Price: %s - %s depth\n", price, formatCount(calculatePriceDepth(price)));
      text += String.format("   %3s %-15s %15s %15s %15s %-15s%n", "#", "Time", "Price", "Amount", "OID", "User ID");

      int i = 1;
      for (val order : priceLevels.get(price)) {
        text += String.format("   %3s %-15s %15.5f %15s %15s %-15s%n",
            i, order.getDateTime().getMillis(), order.getPrice(), formatCount(order.getAmount()), order.getOid(),
            order.getUserId());
        i++;
      }

      text += "\n";
    }

    return text;
  }

  private TreeMultimap<Float, Order> createPriceLevels(OrderSide side) {
    val keyOrder = side == ASK ? Ordering.<Float> natural() : Ordering.<Float> natural().reverse();
    val valueOrder = LimitOrderTimeComparator.INSTANCE;

    return TreeMultimap.create(keyOrder, valueOrder);
  }

}
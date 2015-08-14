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
import static io.fstream.core.model.event.Order.OrderType.LIMIT_ADD;
import static org.assertj.core.api.Assertions.assertThat;
import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Order.OrderType;
import lombok.val;

import org.joda.time.DateTime;
import org.junit.Test;

public class BookSideTest {

  @Test
  public void testIteratorRemove() {
    // Setup
    val orderAmount = 100;
    val order = new Order(ASK, LIMIT_ADD, DateTime.now(), 1, "Fidelity", "RY", orderAmount, 50.0f, "trader1");

    val bookSide = new BookSide(ASK);
    bookSide.addOrder(order);

    val addedDepth = bookSide.getDepth();
    assertThat(addedDepth).isEqualTo(orderAmount);

    val iterator = bookSide.iterator();
    val currentOrder = iterator.next();
    assertThat(currentOrder).isSameAs(order);

    iterator.remove();
    assertThat(bookSide.getDepth()).isEqualTo(0).describedAs("Remove reduced depth");
    assertThat(bookSide.calculateDepth()).isEqualTo(0);
    assertThat(bookSide.calculateOrderCount()).isEqualTo(0);

  }

  @Test
  public void testOrderPutAndCancel() {
    // Setup
    val orderAmount = 100;
    val dateTime = DateTime.now();
    val order1 = new Order(ASK, LIMIT_ADD, dateTime, 1, "Fidelity", "RY", orderAmount, 50.0f, "trader1");
    val order2 = new Order(ASK, LIMIT_ADD, dateTime, 2, "ETrade", "RY", 200, 50.0f, "trader2");

    val bookSide = new BookSide(ASK);
    bookSide.addOrder(order1);
    assertThat(bookSide.getDepth()).isEqualTo(order1.getAmount());
    bookSide.addOrder(order2);
    assertThat(bookSide.calculateDepth()).isEqualTo(order1.getAmount() + order2.getAmount());

    val order3 =
        new Order(ASK, OrderType.LIMIT_CANCEL, dateTime, 1, "Fidelity", "RY", orderAmount, 50.0f, "trader1");
    bookSide.removeOrder(order3);
    assertThat(bookSide.calculateDepth()).isEqualTo(order2.getAmount());
    assertThat(bookSide.getDepth()).isEqualTo(order2.getAmount());
  }

}

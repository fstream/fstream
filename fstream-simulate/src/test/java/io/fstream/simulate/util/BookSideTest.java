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

}

/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import io.fstream.core.model.event.AlertEvent;
import io.fstream.core.model.event.Event;
import io.fstream.core.model.event.MetricEvent;
import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Order.OrderSide;
import io.fstream.core.model.event.Order.OrderType;
import io.fstream.core.model.event.Quote;
import io.fstream.core.model.event.Trade;

import java.util.Arrays;

import lombok.RequiredArgsConstructor;
import lombok.val;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public class CodecTest {

  @Parameters(name = "{index}: event({0})")
  public static Iterable<Object[]> data() {
    return Arrays.asList(new Object[][] {
        { new AlertEvent(new DateTime(), 1, "data") },
        { new MetricEvent(new DateTime(), 1, "data") },
        { new Quote(new DateTime(), "symbol", 0.1f, 0.2f) },
        { new Trade(new DateTime(), "buyer", "seller", 100.0f, true, "RB", 1000) },
        { new Order(OrderSide.ASK, OrderType.LIMIT_ADD, new DateTime(), 123, "xx", "RY", 1000, 10.0f, "retail1") }
    });
  }

  private final Event expected;

  @Test
  public void test() {
    val text = Codec.encodeText(expected);
    System.out.println(text);
    val actual = Codec.decodeText(text, Event.class);

    assertThat(actual).isEqualTo(expected);
  }

}

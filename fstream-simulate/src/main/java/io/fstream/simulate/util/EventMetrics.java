/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.util;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import io.fstream.core.model.event.Event;
import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Quote;
import io.fstream.core.model.event.Snapshot;
import io.fstream.core.model.event.Trade;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import com.codahale.metrics.MetricRegistry;

@RequiredArgsConstructor
public class EventMetrics {

  /**
   * Dependencies.
   */
  @NonNull
  private final MetricRegistry registry;

  public void update(@NonNull Event event) {
    val name = event.getType().name().toLowerCase() + "s";
    val time = event.getDateTime().getMillis();

    // Combined
    registry.meter("events").mark();
    registry.histogram("events" + "." + "time").update(time);

    // By type
    registry.meter(name).mark();
    registry.histogram(name + "." + "time").update(time);

    // By type / symbol
    switch (event.getType()) {
    case TRADE:
      val trade = (Trade) event;

      registry.meter(name + ".symbol." + trade.getSymbol()).mark();
      registry.histogram(name + ".symbol." + trade.getSymbol() + "." + "amount").update(trade.getAmount());
      registry.histogram(name + ".symbol." + trade.getSymbol() + "." + "time").update(time);
      break;

    case ORDER:
      val order = (Order) event;
      val delay = order.getProcessedTime().getMillis() - time;

      registry.meter(name + ".type." + order.getOrderType()).mark();
      registry.meter(name + ".symbol." + order.getSymbol()).mark();
      registry.histogram(name + ".symbol." + order.getSymbol() + "." + "amount").update(order.getAmount());
      registry.histogram(name + ".symbol." + order.getSymbol() + "." + "time").update(time);
      registry.timer(name + ".symbol." + order.getSymbol() + "." + "delay").update(delay, MILLISECONDS);
      break;

    case QUOTE:
      val quote = (Quote) event;

      registry.meter(name + ".symbol." + quote.getSymbol()).mark();
      registry.histogram(name + ".symbol." + quote.getSymbol() + "." + "time").update(time);
      break;

    case SNAPSHOT:
      val snapshot = (Snapshot) event;

      registry.meter(name + ".symbol." + snapshot.getSymbol()).mark();
      registry.histogram(name + ".symbol." + snapshot.getSymbol() + "." + "time").update(time);
      break;

    default:
      break;
    }

  }

}
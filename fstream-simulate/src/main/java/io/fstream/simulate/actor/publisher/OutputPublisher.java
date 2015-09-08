/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.actor.publisher;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import io.fstream.core.model.event.Event;
import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Quote;
import io.fstream.core.model.event.Snapshot;
import io.fstream.core.model.event.Trade;
import io.fstream.simulate.output.Output;

import java.util.List;

import lombok.NonNull;
import lombok.val;
import akka.actor.UntypedActor;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;

public class OutputPublisher extends UntypedActor {

  /**
   * Dependencies
   */
  private final List<Output> outputs;
  private final JmxReporter reporter;

  /**
   * State.
   */
  private final MetricRegistry metrics = new MetricRegistry();

  public OutputPublisher(@NonNull List<Output> outputs) {
    super();
    this.outputs = outputs;
    this.reporter = createReporter(metrics);
    reporter.start();
  }

  @Override
  public void onReceive(Object message) throws Exception {
    val event = (Event) message;

    updateMetrics(event);
    writeEvent(event);
  }

  private void updateMetrics(Event event) {
    val name = event.getType().name().toLowerCase() + "s";
    val time = event.getDateTime().getMillis();

    // Combined
    metrics.meter("events").mark();
    metrics.histogram("events" + "." + "time").update(time);

    // By type
    metrics.meter(name).mark();
    metrics.histogram(name + "." + "time").update(time);

    // By type / symbol
    switch (event.getType()) {
    case TRADE:
      val trade = (Trade) event;

      metrics.meter(name + ".symbol." + trade.getSymbol()).mark();
      metrics.histogram(name + ".symbol." + trade.getSymbol() + "." + "amount").update(trade.getAmount());
      metrics.histogram(name + ".symbol." + trade.getSymbol() + "." + "time").update(time);
      break;

    case ORDER:
      val order = (Order) event;
      val delay = order.getProcessedTime().getMillis() - time;

      metrics.meter(name + ".type." + order.getOrderType()).mark();
      metrics.meter(name + ".symbol." + order.getSymbol()).mark();
      metrics.histogram(name + ".symbol." + order.getSymbol() + "." + "amount").update(order.getAmount());
      metrics.histogram(name + ".symbol." + order.getSymbol() + "." + "time").update(time);
      metrics.timer(name + ".symbol." + order.getSymbol() + "." + "delay").update(delay, MILLISECONDS);
      break;

    case QUOTE:
      val quote = (Quote) event;

      metrics.meter(name + ".symbol." + quote.getSymbol()).mark();
      metrics.histogram(name + ".symbol." + quote.getSymbol() + "." + "time").update(time);
      break;

    case SNAPSHOT:
      val snapshot = (Snapshot) event;

      metrics.meter(name + ".symbol." + snapshot.getSymbol()).mark();
      metrics.histogram(name + ".symbol." + snapshot.getSymbol() + "." + "time").update(time);
      break;

    default:
      break;
    }
  }

  private void writeEvent(Event event) {
    for (val output : outputs) {
      output.write(event);
    }
  }

  private static JmxReporter createReporter(MetricRegistry metrics) {
    return JmxReporter.forRegistry(metrics).inDomain("io.fstream.simulate.publisher").build();
  }

}

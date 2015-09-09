/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.actor.publisher;

import io.fstream.core.model.event.Event;
import io.fstream.simulate.output.Output;
import io.fstream.simulate.util.EventMetrics;

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
  private final EventMetrics metrics;

  /**
   * State.
   */
  private final MetricRegistry registry = new MetricRegistry();

  public OutputPublisher(@NonNull List<Output> outputs) {
    this.outputs = outputs;
    this.reporter = createReporter(registry);
    this.metrics = new EventMetrics(registry);

    reporter.start();
  }

  @Override
  public void onReceive(Object message) throws Exception {
    val event = (Event) message;

    updateMetrics(event);
    writeEvent(event);
  }

  private void updateMetrics(Event event) {
    metrics.update(event);
  }

  private void writeEvent(Event event) {
    for (val output : outputs) {
      output.write(event);
    }
  }

  private static JmxReporter createReporter(MetricRegistry registry) {
    return JmxReporter.forRegistry(registry).inDomain(OutputPublisher.class.getName()).build();
  }

}

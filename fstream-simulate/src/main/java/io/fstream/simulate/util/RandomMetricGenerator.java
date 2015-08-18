/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.util;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import io.fstream.core.model.event.MetricEvent;

import java.util.Random;

import lombok.RequiredArgsConstructor;
import lombok.val;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.joda.time.DateTime;

import com.google.common.collect.Maps;

/**
 * {@link QuoteEvent} generator used to create a simulated quote feed when running in simulation mode.
 */
@RequiredArgsConstructor
public class RandomMetricGenerator implements Processor {

  /**
   * Configuration.
   */
  private final int id;

  /**
   * State.
   */
  private final Random random = new Random();

  @Override
  public void process(Exchange exchange) throws Exception {
    val delay = generateDelay(0, 30 * 1000);
    MILLISECONDS.sleep(delay);

    val data = Maps.newHashMap();
    val metric = new MetricEvent(DateTime.now(), id, data);

    // Simulated event
    exchange.getOut().setBody(metric);
  }

  private int generateDelay(int min, int max) {
    return (int) (min + (max - min) * random.nextFloat());
  }

}
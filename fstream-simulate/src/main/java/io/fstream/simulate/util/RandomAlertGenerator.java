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
import io.fstream.core.model.event.AlertEvent;

import java.util.Random;

import lombok.RequiredArgsConstructor;
import lombok.val;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.assertj.core.util.Maps;
import org.joda.time.DateTime;

/**
 * {@link QuoteEvent} generator used to create a simulated quote feed when running in simulation mode.
 */
@RequiredArgsConstructor
public class RandomAlertGenerator implements Processor {

  /**
   * Configuration.
   */
  private final int id;
  private final String symbol;

  /**
   * State.
   */
  private final Random random = new Random();

  @Override
  public void process(Exchange exchange) throws Exception {
    val delay = generateDelay(0, 1 * 1000);
    MILLISECONDS.sleep(delay);

    val data = Maps.newHashMap();
    val alert = new AlertEvent(DateTime.now(), id, symbol, data);

    // Simulated event
    exchange.getOut().setBody(alert);
  }

  private int generateDelay(int min, int max) {
    return (int) (min + (max - min) * random.nextFloat());
  }

}
/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.handler;

import io.fstream.core.model.event.TickEvent;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import lombok.val;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.joda.time.DateTime;

public class RandomRates implements Processor {

  /**
   * Configuration.
   */
  private final String symbol = "EUR/USD";

  /**
   * State.
   */
  private final Random random = new Random();

  @Override
  public void process(Exchange exchange) throws Exception {
    // Random pricing
    val price = generatePrice(1.2f, 1.5f);
    val ask = price + 0.01f;
    val bid = price - 0.01f;
    val event = new TickEvent(new DateTime(), symbol, ask, bid);

    // Random timing
    val delay = generateDelay(0, 10);
    TimeUnit.SECONDS.sleep(delay);

    // Simulated event

    exchange.getOut().setBody(event);
  }

  private float generatePrice(float min, float max) {
    return min + (max - min) * random.nextFloat();
  }

  private int generateDelay(int min, int max) {
    return (int) (min + (max - min) * random.nextFloat());
  }

}
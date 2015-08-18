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

import java.util.List;
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
public class RandomAlertGenerator implements Processor {

  /**
   * Configuration.
   */
  private final List<Integer> ids;
  private final List<String> symbol;

  /**
   * State.
   */
  private final Random random = new Random();

  @Override
  public void process(Exchange exchange) throws Exception {
    val delay = generateDelay(0, 1 * 1000);
    MILLISECONDS.sleep(delay);

    val data = Maps.newHashMap();
    val alert = new AlertEvent(DateTime.now(), generateId(), generateSymbol(), data);

    // Simulated event
    exchange.getOut().setBody(alert);
  }

  private int generateDelay(int min, int max) {
    return randomInt(min, max);
  }

  private String generateSymbol() {
    return randomElement(symbol);
  }

  private int generateId() {
    return randomElement(ids);
  }

  /**
   * Return a random element in {@code list}.
   */
  protected <T> T randomElement(List<T> list) {
    val randomIndex = random.nextInt(list.size());
    return list.get(randomIndex);
  }

  /**
   * Return a random number in range {@code [min, max]}
   */
  protected int randomInt(int min, int max) {
    return min + random.nextInt(max - min) + 1;
  }

}
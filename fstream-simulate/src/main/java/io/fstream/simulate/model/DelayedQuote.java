/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.model;

import io.fstream.core.model.event.Quote;

import org.joda.time.DateTime;

/**
 * Represents a delayed quote message.
 * <p>
 * Same as in all respects but elicit different behavior from the consumer of message.
 */
// TODO: Can this class be removed if we can simulate the delay in the producer?
public class DelayedQuote extends Quote {

  public DelayedQuote(DateTime time, String symbol, float ask, float bid, int askDepth, int bidDepth) {
    super(time, symbol, ask, bid, askDepth, bidDepth);
  }

}

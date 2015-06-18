/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.orders;

import org.joda.time.DateTime;

/**
 * Represents a delayed quote message. Same as in all respects but elicit different behaviour from the consumer of
 * message
 */
public class DelayedQuote extends Quote {

  public DelayedQuote(DateTime time, String symbol, float ask, float bid, int askdepth, int biddepth) {
    super(time, symbol, ask, bid, askdepth, biddepth);
  }

}

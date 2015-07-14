/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.util;

import io.fstream.simulate.actor.OrderBook;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OrderBookFormatter {

  public static String formatOrderBook(@NonNull OrderBook orderBook) {
    String text = String.format("BOOK = %s\n", orderBook.getSymbol());

    text += "------ ASKS -------\n";
    text += orderBook.getAsks();

    text += "------ BIDS -------\n";
    text += orderBook.getBids();

    text += String.format("bid depth = %s, ask depth = %s\n",
        orderBook.getBids().getDepth(), orderBook.getAsks().getDepth());
    text += String.format("best ask = %s, best bid = %s, spread = %s\n",
        orderBook.getBestAsk(), orderBook.getBestBid(), orderBook.getSpread());

    text += "----- END -----\n";

    return text;
  }

}

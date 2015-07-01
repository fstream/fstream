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
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OrderBookFormatter {

  public static String formatOrderBook(@NonNull OrderBook orderBook) {
    String text = String.format("BOOK = %s\n", orderBook.getSymbol());

    text += "------ ASKS -------\n";
    for (val ask : orderBook.getAsks().entrySet()) {
      text += String.format("%s -> ", ask.getKey());
      for (val firstnode : ask.getValue()) {
        text += String.format("( %s,%s,%s) -> ", firstnode.getSentTime().toString(), firstnode.getPrice(),
            firstnode.getAmount());
      }
      text += "\n";
    }

    text += "------ BIDS -------\n";
    for (val bid : orderBook.getBids().entrySet()) {
      text += String.format("%s -> ", bid.getKey());
      for (val firstOrder : bid.getValue()) {
        text += String.format("( %s,%s,%s) -> ",
            firstOrder.getSentTime().toString(), firstOrder.getPrice(), firstOrder.getAmount());
      }
      text = text + "\n";
    }

    text += String.format("bid depth = %s, ask depth = %s\n",
        orderBook.getBidDepth(), orderBook.getAskDepth());
    text += String.format("best ask = %s, best bid =%s, spread = %s\n",
        orderBook.getBestAsk(), orderBook.getBestBid(), orderBook.getBestAsk() - orderBook.getBestBid());

    text += "----- END -----\n";

    return text;
  }

}

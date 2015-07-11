/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.feed.util;

import io.fstream.core.model.event.Quote;
import lombok.SneakyThrows;
import lombok.val;

import org.apache.camel.Exchange;
import org.apache.camel.TypeConversionException;
import org.apache.camel.support.TypeConverterSupport;
import org.joda.time.DateTime;

import quickfix.field.MDEntryPx;
import quickfix.field.MDEntryType;
import quickfix.field.SendingTime;
import quickfix.fix44.MarketDataSnapshotFullRefresh;

/**
 * Converts FIX messages to fStream quotes.
 * 
 * @see http
 * ://algo-trader.googlecode.com/svn-history/r539/branches/fix-md/code/src/main/java/com/algoTrader/service/fix/
 * FixMessageHandler.java
 */
public class QuoteEventTypeConverter extends TypeConverterSupport {

  @Override
  @SuppressWarnings("unchecked")
  public <T> T convertTo(Class<T> type, Exchange exchange, Object value) throws TypeConversionException {
    val message = (MarketDataSnapshotFullRefresh) value;
    val quote = convertTo(message);

    return (T) quote;
  }

  @SneakyThrows
  private Quote convertTo(MarketDataSnapshotFullRefresh message) {
    val quote = new Quote();
    quote.setSymbol(message.getSymbol().getValue());
    quote.setDateTime(new DateTime(message.getHeader().getUtcTimeStamp(SendingTime.FIELD)));

    val entryCount = message.getNoMDEntries().getValue();
    for (int i = 0; i < entryCount; i++) {
      val group = new MarketDataSnapshotFullRefresh.NoMDEntries();
      message.getGroup(i + 1, group);

      val entryType = group.getMDEntryType().getValue();
      if (entryType == MDEntryType.BID) {
        quote.setBid((float) group.getDouble(MDEntryPx.FIELD));
      } else if (entryType == MDEntryType.OFFER) {
        quote.setAsk((float) group.getDouble(MDEntryPx.FIELD));
      }
    }

    quote.setMid((quote.getAsk() + quote.getBid()) / 2.0f);

    return quote;
  }

}

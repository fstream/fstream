/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.util;

import io.fstream.core.model.event.QuoteEvent;
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
 * Converts FIX messages to fStream rates.
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
    val rate = convertTo(message);

    return (T) rate;
  }

  @SneakyThrows
  private QuoteEvent convertTo(MarketDataSnapshotFullRefresh message) {
    val event = new QuoteEvent();
    event.setSymbol(message.getSymbol().getValue());
    event.setDateTime(new DateTime(message.getHeader().getUtcTimeStamp(SendingTime.FIELD)));

    val entryCount = message.getNoMDEntries().getValue();
    for (int i = 0; i < entryCount; i++) {
      val group = new MarketDataSnapshotFullRefresh.NoMDEntries();
      message.getGroup(i + 1, group);

      val entryType = group.getMDEntryType().getValue();
      if (entryType == MDEntryType.BID) {
        event.setBid((float) group.getDouble(MDEntryPx.FIELD));
      } else if (entryType == MDEntryType.OFFER) {
        event.setAsk((float) group.getDouble(MDEntryPx.FIELD));
      }
    }

    event.setMid((event.getAsk() + event.getBid()) / 2.0f);

    return event;
  }

}
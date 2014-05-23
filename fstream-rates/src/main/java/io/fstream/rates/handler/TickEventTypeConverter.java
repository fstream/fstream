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
public class TickEventTypeConverter extends TypeConverterSupport {

  @Override
  @SuppressWarnings("unchecked")
  public <T> T convertTo(Class<T> type, Exchange exchange, Object value) throws TypeConversionException {
    val message = (MarketDataSnapshotFullRefresh) value;
    val rate = convertTo(message);

    return (T) rate;
  }

  @SneakyThrows
  private TickEvent convertTo(MarketDataSnapshotFullRefresh message) {
    val event = new TickEvent();
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

    return event;
  }

}

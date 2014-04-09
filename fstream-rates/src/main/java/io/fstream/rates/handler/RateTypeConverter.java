/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.handler;

import io.fstream.core.model.Rate;
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
 * Converts FIX to fStream.
 * @see http
 * ://algo-trader.googlecode.com/svn-history/r539/branches/fix-md/code/src/main/java/com/algoTrader/service/fix/
 * FixMessageHandler.java
 */
public class RateTypeConverter extends TypeConverterSupport {

  @SneakyThrows
  @Override
  @SuppressWarnings("unchecked")
  public <T> T convertTo(Class<T> type, Exchange exchange, Object value) throws TypeConversionException {
    val message = (MarketDataSnapshotFullRefresh) value;

    val rate = new Rate();
    rate.setSymbol(message.getSymbol().getValue());
    rate.setDateTime(new DateTime(message.getHeader().getUtcTimeStamp(SendingTime.FIELD)));

    val entryCount = message.getNoMDEntries().getValue();
    for (int i = 0; i < entryCount; i++) {
      val group = new MarketDataSnapshotFullRefresh.NoMDEntries();
      message.getGroup(i + 1, group);

      val entryType = group.getMDEntryType().getValue();
      if (entryType == MDEntryType.BID) {
        rate.setBid(group.getDecimal(MDEntryPx.FIELD));
      } else if (entryType == MDEntryType.OFFER) {
        rate.setAsk(group.getDecimal(MDEntryPx.FIELD));
      }
    }

    return (T) rate;
  }

}

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

import quickfix.field.MDEntryPx;
import quickfix.field.MDEntryType;
import quickfix.fix44.MarketDataSnapshotFullRefresh;

/**
 * Converts FIX to fStream.
 */
public class RateTypeConverter extends TypeConverterSupport {

  @SneakyThrows
  @Override
  @SuppressWarnings("unchecked")
  public <T> T convertTo(Class<T> type, Exchange exchange, Object value) throws TypeConversionException {
    val message = (MarketDataSnapshotFullRefresh) value;
    val rate = new Rate();
    rate.setSymbol(message.getSymbol().getValue());

    val noEntries = message.getNoMDEntries().getValue();
    for (int i = 0; i < noEntries; i++) {
      MarketDataSnapshotFullRefresh.NoMDEntries group = new MarketDataSnapshotFullRefresh.NoMDEntries();
      message.getGroup(i + 1, group);

      char currentEntryType = group.getChar(MDEntryType.FIELD);
      if (currentEntryType == MDEntryType.BID) {
        rate.setBid(group.getDecimal(MDEntryPx.FIELD));
      } else if (currentEntryType == MDEntryType.OFFER) {
        rate.setAsk(group.getDecimal(MDEntryPx.FIELD));
      }
    }

    return (T) rate;
  }

}

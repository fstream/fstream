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

    return (T) rate;
  }

}

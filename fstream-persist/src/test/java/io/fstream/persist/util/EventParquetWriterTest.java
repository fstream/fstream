/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist.util;

import static io.fstream.core.model.event.EventType.ORDER;
import static io.fstream.core.model.event.EventType.QUOTE;
import static io.fstream.core.model.event.EventType.TRADE;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Quote;
import io.fstream.core.model.event.Trade;

import java.io.File;

import lombok.Cleanup;
import lombok.val;

import org.junit.Before;
import org.junit.Test;

public class EventParquetWriterTest {

  String tradesFile = "build/trades.parquet";
  String ordersFile = "build/orders.parquet";
  String quotesFile = "build/quotes.parquet";

  @Before
  public void setUp() {
    deleteQuietly(new File(tradesFile));
    deleteQuietly(new File(ordersFile));
    deleteQuietly(new File(quotesFile));
  }

  @Test
  public void testWrite() throws Exception {
    @Cleanup
    val tradesWriter = new EventParquetWriter(TRADE, tradesFile);
    tradesWriter.write(new Trade());
    @Cleanup
    val ordersWriter = new EventParquetWriter(ORDER, ordersFile);
    ordersWriter.write(new Order());
    @Cleanup
    val quotesWriter = new EventParquetWriter(QUOTE, quotesFile);
    quotesWriter.write(new Quote());
  }

}

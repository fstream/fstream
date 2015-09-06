/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist.parquet;

import static io.fstream.core.model.event.EventType.ORDER;
import static io.fstream.core.model.event.EventType.QUOTE;
import static io.fstream.core.model.event.EventType.TRADE;
import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Quote;
import io.fstream.core.model.event.Trade;
import io.fstream.persist.parquet.EventParquetWriter;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.junit.Before;
import org.junit.Test;

public class EventParquetWriterTest {

  Path tradesFile = new Path("build/trades.parquet");
  Path ordersFile = new Path("build/orders.parquet");
  Path quotesFile = new Path("build/quotes.parquet");

  @Before
  public void setUp() {
    deleteQuietly(tradesFile);
    deleteQuietly(ordersFile);
    deleteQuietly(quotesFile);
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

  @SneakyThrows
  private static void deleteQuietly(Path file) {
    file.getFileSystem(new Configuration()).delete(file, false);
  }

}

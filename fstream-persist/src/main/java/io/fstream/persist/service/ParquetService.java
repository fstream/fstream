/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist.service;

import static io.fstream.core.model.event.EventType.ORDER;
import static io.fstream.core.model.event.EventType.QUOTE;
import static io.fstream.core.model.event.EventType.TRADE;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import io.fstream.core.model.event.Event;
import io.fstream.core.model.event.EventType;
import io.fstream.persist.util.EventParquetWriter;

import java.io.File;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("parquet")
public class ParquetService implements PersistenceService {

  /**
   * Configuration.
   */
  @Value("${parquet.files.trades}")
  private String tradesFileName;
  @Value("${parquet.files.orders}")
  private String ordersFileName;
  @Value("${parquet.files.quotes}")
  private String quotesFileName;

  /**
   * State.
   */
  private EventParquetWriter tradesWriter;
  private EventParquetWriter ordersWriter;
  private EventParquetWriter quotesWriter;

  @PostConstruct
  public void initialize() {
    this.tradesWriter = createWriter(TRADE, tradesFileName);
    this.ordersWriter = createWriter(ORDER, ordersFileName);
    this.quotesWriter = createWriter(QUOTE, quotesFileName);
  }

  @PreDestroy
  public void destroy() throws Exception {
    tradesWriter.close();
    ordersWriter.close();
    quotesWriter.close();
  }

  @Override
  public void persist(Event event) {
    switch (event.getType()) {
    case TRADE:
      tradesWriter.write(event);
      break;
    case ORDER:
      ordersWriter.write(event);
      break;
    case QUOTE:
      quotesWriter.write(event);
      break;

    default:
      break;
    }
  }

  @SneakyThrows
  private static EventParquetWriter createWriter(EventType type, String fileName) {
    val file = new File(fileName);
    log.info("Writing to '{}'", file.getCanonicalPath());
    deleteQuietly(file);

    return new EventParquetWriter(type, fileName);
  }

}

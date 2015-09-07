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
import io.fstream.core.model.event.Event;
import io.fstream.core.model.event.EventType;
import io.fstream.persist.core.PersistenceService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
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
  @Value("${persist.file.dir}")
  private String fileDir;

  /**
   * State.
   */
  private EventParquetWriter tradesWriter;
  private EventParquetWriter ordersWriter;
  private EventParquetWriter quotesWriter;

  @PostConstruct
  public void initialize() {
    log.info("Creating writers...");
    this.tradesWriter = createWriter(TRADE, new Path(fileDir, "fstream-trades.parquet"));
    this.ordersWriter = createWriter(ORDER, new Path(fileDir, "fstream-orders.parquet"));
    this.quotesWriter = createWriter(QUOTE, new Path(fileDir, "fstream-quotes.parquet"));
    log.info("Finished creating writers");
  }

  @PreDestroy
  public void destroy() throws Exception {
    log.info("Closing writers...");
    tradesWriter.close();
    ordersWriter.close();
    quotesWriter.close();
    log.info("Finished closing writers");
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
  private static EventParquetWriter createWriter(EventType type, Path outputFile) {
    log.info("Writing '{}' events to '{}'", type, outputFile);
    outputFile.getFileSystem(new Configuration()).delete(outputFile, false);

    return new RollingEventParquetWriter(type, outputFile, 10_000_000);
  }

}

/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist.service;

import static io.fstream.core.model.event.EventType.ALERT;
import static io.fstream.core.model.event.EventType.METRIC;
import static io.fstream.core.model.event.EventType.ORDER;
import static io.fstream.core.model.event.EventType.QUOTE;
import static io.fstream.core.model.event.EventType.TRADE;
import io.fstream.core.model.event.AlertEvent;
import io.fstream.core.model.event.Event;
import io.fstream.core.model.event.MetricEvent;
import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Quote;
import io.fstream.core.model.event.Trade;
import io.fstream.core.util.Codec;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Serie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("influxdb")
public class InfluxDBService implements PersistenceService {

  /**
   * Constants.
   */
  private static final TimeUnit PRECISION = TimeUnit.MILLISECONDS;

  /**
   * Configuration.
   */
  @Value("${influxdb.database}")
  private String databaseName;

  /**
   * Dependencies
   */
  @Autowired
  private InfluxDB influxDb;

  @PostConstruct
  public void initialize() {
    if (!isDBExists()) {
      // Doesn't exist
      log.info("Initializing database '{}'...", databaseName);
      influxDb.createDatabase(databaseName);

      // Register "fanout continuous query"
      influxDb.query(databaseName,
          "SELECT ask, bid FROM quotes INTO quotes.[symbol]", PRECISION);

      // Register "downsampling continuous query"
      influxDb
          .query(
              databaseName,
              "SELECT MEAN(ask) AS ask, MEAN(bid) AS bid FROM /^quotes\\..*/ GROUP BY time(1m) INTO rollups.1m.:series_name",
              PRECISION);
      influxDb.query(databaseName,
          "SELECT MEAN(ask), MEAN(bid) FROM /^quotes\\..*/ GROUP BY time(1h) INTO rollups.1h.:series_name", PRECISION);
    }
  }

  @Override
  public void persist(Event event) {
    val serie = createSerie(event);

    influxDb.write(databaseName, PRECISION, serie);
  }

  private Serie createSerie(Event event) {
    if (event.getType() == TRADE) {
      val trade = (Trade) event;
      return new Serie.Builder("trades")
          .columns("time", "symbol", "amount", "price", "buyUser", "sellUser", "activeBuy")
          .values(event.getDateTime().getMillis(), trade.getSymbol(), trade.getAmount(), trade.getPrice(),
              trade.getBuyUser(), trade.getSellUser(), trade.isActiveBuy())
          .build();
    } else if (event.getType() == ORDER) {
      val order = (Order) event;
      return new Serie.Builder("orders")
          .columns("time", "symbol", "amount", "price", "oid", "orderType", "side", "brokerId", "userId",
              "processedTime")
          .values(event.getDateTime().getMillis(), order.getSymbol(), order.getAmount(), order.getPrice(),
              order.getOid(), order.getOrderType(), order.getSide(), order.getBrokerId(), order.getUserId(),
              order.getProcessedTime().getMillis())
          .build();
    } else if (event.getType() == QUOTE) {
      val quote = (Quote) event;
      return new Serie.Builder("quotes")
          .columns("time", "symbol", "ask", "bid")
          .values(event.getDateTime().getMillis(), quote.getSymbol(), quote.getAsk(), quote.getBid())
          .build();
    } else if (event.getType() == METRIC) {
      val metricEvent = (MetricEvent) event;
      return new Serie.Builder("metrics")
          .columns("time", "id", "data")
          .values(event.getDateTime().getMillis(), metricEvent.getId(), Codec.encodeText(metricEvent.getData()))
          .build();
    } else if (event.getType() == ALERT) {
      val alertEvent = (AlertEvent) event;
      return new Serie.Builder("alerts")
          .columns("time", "id", "data")
          .values(event.getDateTime().getMillis(), alertEvent.getId(), Codec.encodeText(alertEvent.getData()))
          .build();
    }

    return null;
  }

  @SuppressWarnings("unused")
  private void dropContinuousQueries() {
    // See https://github.com/influxdb/influxdb-java/issues/30
    for (val continuousQuery : influxDb.describeContinuousQueries(databaseName)) {
      influxDb.deleteContinuousQuery(databaseName, continuousQuery.getId());
    }
  }

  private boolean isDBExists() {
    for (val database : influxDb.describeDatabases()) {
      val exists = database.getName().equals(databaseName);
      if (exists) {
        return true;
      }
    }

    return false;
  }

}

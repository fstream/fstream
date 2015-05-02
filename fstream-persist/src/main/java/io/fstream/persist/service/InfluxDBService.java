/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist.service;

import io.fstream.core.model.event.AlertEvent;
import io.fstream.core.model.event.Event;
import io.fstream.core.model.event.EventType;
import io.fstream.core.model.event.MetricEvent;
import io.fstream.core.model.event.TickEvent;

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
          "SELECT ask, bid FROM ticks INTO ticks.[symbol]", PRECISION);

      // Register "downsampling continuous query"
      influxDb
          .query(
              databaseName,
              "SELECT MEAN(ask) AS ask, MEAN(bid) AS bid FROM /^ticks\\..*/ GROUP BY time(1m) INTO rollups.1m.:series_name",
              PRECISION);
      influxDb.query(databaseName,
          "SELECT MEAN(ask), MEAN(bid) FROM /^ticks\\..*/ GROUP BY time(1h) INTO rollups.1h.:series_name", PRECISION);
    }
  }

  @Override
  public void persist(Event event) {
    val serie = createSerie(event);

    influxDb.write(databaseName, PRECISION, serie);
  }

  private Serie createSerie(Event event) {
    if (event.getType() == EventType.TICK) {
      val tickEvent = (TickEvent) event;
      return new Serie.Builder("ticks")
          .columns("time", "symbol", "ask", "bid")
          .values(event.getDateTime().getMillis(), tickEvent.getSymbol(), tickEvent.getAsk(), tickEvent.getBid())
          .build();
    } else if (event.getType() == EventType.METRIC) {
      val metricEvent = (MetricEvent) event;
      return new Serie.Builder("metrics")
          .columns("time", "id", "data")
          .values(event.getDateTime().getMillis(), metricEvent.getId(), metricEvent.getData().toString())
          .build();
    } else if (event.getType() == EventType.ALERT) {
      val alertEvent = (AlertEvent) event;
      return new Serie.Builder("alerts")
          .columns("time", "id", "data")
          .values(event.getDateTime().getMillis(), alertEvent.getId(), alertEvent.getData().toString())
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

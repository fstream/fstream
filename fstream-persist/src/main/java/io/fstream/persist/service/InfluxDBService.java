/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist.service;

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

    for (val database : influxDb.describeDatabases()) {
      val exists = database.getName().equals(databaseName);
      if (exists) {
        return;
      }
    }

    // Doesn't exist
    log.info("Initializing database '{}'...", databaseName);
    influxDb.createDatabase(databaseName);
  }

  @Override
  public void persist(TickEvent event) {
    val serie = createSerie(event);

    influxDb.write(databaseName, TimeUnit.MILLISECONDS, serie);
  }

  private Serie createSerie(TickEvent event) {
    return new Serie.Builder(event.getSymbol())
        .columns("time", "ask", "bid")
        .values(event.getDateTime().getMillis(), event.getAsk(), event.getBid())
        .build();
  }

}

/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.web.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.NonNull;
import lombok.val;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HistoryService {

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

  public List<Result> executeQuery(@NonNull String text) {
    val query = new Query(text, databaseName);
    val result = influxDb.query(query, PRECISION);

    return result.getResults();
  }

}

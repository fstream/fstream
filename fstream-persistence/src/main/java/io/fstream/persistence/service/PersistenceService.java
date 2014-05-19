/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persistence.service;

import io.fstream.core.model.Rate;

import javax.annotation.PostConstruct;

import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PersistenceService {

  /**
   * Constants.
   */
  private static final String TABLENAME = "oanda";
  private static final String CFDATA = "data";
  private static final String CFMETA = "meta";

  /**
   * Dependencies.
   */
  @Setter
  @Autowired
  private HBaseAdmin admin;

  @SneakyThrows
  @PostConstruct
  private void initializeTable() {
    val tableName = TABLENAME;
    log.info("Initializing table '{}'...", tableName);

    if (admin.tableExists(tableName)) {
      if (admin.isTableDisabled(tableName)) {
        // Assuming table schema is defined as per expected
        admin.enableTable(tableName);
      }
      log.info("Table '{}' exists and is enabled", tableName);
    } else {
      createTable(tableName);
    }
  }

  @SneakyThrows
  public void persist(Rate rate) {
    // TODO: Use connection pooling
    val connection = HConnectionManager.createConnection(admin.getConfiguration());
    val table = connection.getTable(TABLENAME);

    try {
      val key = rate.getDateTime().getMillis() + rate.getSymbol();

      val row = new Put(Bytes.toBytes(key));
      row.add(Bytes.toBytes(CFDATA), Bytes.toBytes("Price:bid"),
          Bytes.toBytes(rate.getBid()));
      row.add(Bytes.toBytes(CFDATA), Bytes.toBytes("Price:ask"),
          Bytes.toBytes(rate.getAsk()));
      row.add(Bytes.toBytes(CFDATA), Bytes.toBytes("symbol"),
          Bytes.toBytes(rate.getSymbol()));

      log.info("**** Putting row");
      table.put(row);
    } finally {
      table.close();
      connection.close();
    }
  }

  @SneakyThrows
  private void createTable(String tableName) {
    val descriptor = new HTableDescriptor(TableName.valueOf(tableName));
    descriptor.addFamily(new HColumnDescriptor(CFDATA));
    descriptor.addFamily(new HColumnDescriptor(CFMETA));

    log.info("Creating table '{}'...", tableName);
    admin.createTable(descriptor);
    log.info("Created table '{}'", tableName);
  }

}

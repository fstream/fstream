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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
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
  private static Configuration config;
  @Setter
  @Autowired
  private HBaseAdmin admin;

  /**
   * State.
   */
  private HTable table;

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

    table = new HTable(config, tableName);
  }

  @SneakyThrows
  public void addRow(Rate rate) {
    val key = rate.getDateTime().getMillis() + rate.getSymbol();

    val row = new Put(Bytes.toBytes(key));
    row.add(Bytes.toBytes(CFDATA), Bytes.toBytes("Price:bid"),
        Bytes.toBytes(rate.getBid()));
    row.add(Bytes.toBytes(CFDATA), Bytes.toBytes("Price:ask"),
        Bytes.toBytes(rate.getAsk()));
    row.add(Bytes.toBytes(CFDATA), Bytes.toBytes("symbol"),
        Bytes.toBytes(rate.getSymbol()));

    table.put(row);
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

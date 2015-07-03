/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist.service;

import io.fstream.core.model.event.Event;
import io.fstream.core.model.event.QuoteEvent;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Service responsible for persisting to the long-term backing store.
 * <p>
 * This class is <em>not</em> thread-safe.
 */
@Slf4j
@Service
@Profile("hbase")
public class HBaseService implements PersistenceService {

  /**
   * Constants.
   */
  private static final TableName TABLE_NAME = TableName.valueOf("oanda");
  private static final byte[] CF_DATA = Bytes.toBytes("data");
  private static final byte[] CF_META = Bytes.toBytes("meta");
  private static final byte[] COLUMN_BID = Bytes.toBytes("Price:bid");
  private static final byte[] COLUMN_ASK = Bytes.toBytes("Price:ask");
  private static final byte[] COLUMN_SYMBOL = Bytes.toBytes("symbol");

  /**
   * Dependencies.
   */
  @Setter
  @Autowired
  private Admin admin;

  private Connection connection;

  @PostConstruct
  public void initialize() {
    log.info("Initializing table '{}'...", TABLE_NAME.getNameAsString());

    if (tableExists()) {
      enableTable();
    } else {
      createTable();
    }

    connect();
  }

  @SneakyThrows
  @PreDestroy
  public void destroy() {
    if (connection != null) {
      connection.close();
    }
  }

  @Override
  @SneakyThrows
  public void persist(Event event) {
    val table = connection.getTable(TABLE_NAME);
    try {
      // TODO: Support other types!
      val quoteEvent = (QuoteEvent) event;
      val key = createKey(quoteEvent);

      val row = new Put(Bytes.toBytes(key));
      row.addColumn(CF_DATA, COLUMN_BID, Bytes.toBytes(quoteEvent.getBid()));
      row.addColumn(CF_DATA, COLUMN_ASK, Bytes.toBytes(quoteEvent.getAsk()));
      row.addColumn(CF_DATA, COLUMN_SYMBOL, Bytes.toBytes(quoteEvent.getSymbol()));

      log.info("**** Putting row");
      table.put(row);
    } finally {
      table.close();
    }
  }

  private void connect() {
    this.connection = createConnection();
  }

  @SneakyThrows
  private boolean tableExists() {
    return admin.tableExists(TABLE_NAME);
  }

  @SneakyThrows
  private void createTable() {
    val descriptor = new HTableDescriptor(TABLE_NAME);
    descriptor.addFamily(new HColumnDescriptor(CF_DATA));
    descriptor.addFamily(new HColumnDescriptor(CF_META));

    log.info("Creating table '{}'...", TABLE_NAME.getNameAsString());
    admin.createTable(descriptor);
    log.info("Tabled created.");
  }

  @SneakyThrows
  private void enableTable() {
    if (admin.isTableDisabled(TABLE_NAME)) {
      // Assuming table schema is defined as per expected
      admin.enableTable(TABLE_NAME);
    }

    log.info("Table '{}' exists and is enabled", TABLE_NAME.getNameAsString());
  }

  private String createKey(QuoteEvent rate) {
    val hourFloor = rate.getDateTime().hourOfDay().roundFloorCopy();

    return hourFloor.getMillis() + rate.getSymbol();
  }

  @SneakyThrows
  private Connection createConnection() {
    log.info("Creating connection...");
    val connection = ConnectionFactory.createConnection(admin.getConfiguration());
    log.info("Connection created.");

    return connection;
  }

}

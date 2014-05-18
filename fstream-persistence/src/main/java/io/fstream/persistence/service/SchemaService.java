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

import java.text.SimpleDateFormat;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SchemaService {

  private static String TABLENAME = "oanda";
  private static String CFDATA = "data";
  private static String CFMETA = "meta";
  private static Configuration config;
  private static HTable table;

  private static final String[] sampledata = { "1399538895", "EURUSD", "1.0005", "1.0004", "QUOTE" };
  SimpleDateFormat timeformat = new SimpleDateFormat("dd MMM yyyy hh:mm:ss.S");

  public SchemaService() {
    // set
    config = HBaseConfiguration.create();
    initializeTable(TABLENAME);
  }

  public static void main(String[] args) {
     new SchemaService();
  }
  
  @SneakyThrows
  private void initializeTable (String tablename) {
    log.info("connected to hbase");
    HBaseAdmin admin = new HBaseAdmin(config);
    if (admin.tableExists(tablename)) {
      if (admin.isTableDisabled(tablename)) { // assuming table schema is defined as per expected
        admin.enableTable(tablename);
      }
      log.info("table {} exists and is enabled",tablename);
    }
    else { // create table
      HTableDescriptor tdescriptor = new HTableDescriptor(TableName.valueOf(tablename));
      tdescriptor.addFamily(new HColumnDescriptor(CFDATA));
      tdescriptor.addFamily(new HColumnDescriptor(CFMETA));
      admin.createTable(tdescriptor);
      log.info("created table " + tablename);
      // admin.addColumn(tablename, new HColumnDescriptor(CFDATA));
      admin.close();
    }
   table = new HTable(config, tablename);
  }

  @SneakyThrows
  private void populateTable(String tablename) {
    HTable table = new HTable(config, tablename);
    DateTime time = new DateTime(Long.parseLong(sampledata[0]) * 1000);
    DateTime timerounded = time.hourOfDay().roundFloorCopy();

    Put row = new Put(Bytes.toBytes(timerounded.getMillis() + sampledata[1]));
    row.add(Bytes.toBytes(CFDATA), Bytes.toBytes("Price:bid"),
        Bytes.toBytes(sampledata[2]));
    row.add(Bytes.toBytes(CFDATA), Bytes.toBytes("Price:ask"),
        Bytes.toBytes(sampledata[3]));
    table.put(row);
  }
  
  @SneakyThrows
  public void addRow(Rate rate) {
    val row = new Put(Bytes.toBytes(rate.getDateTime().getMillis() + rate.getSymbol()));
    row.add(Bytes.toBytes(CFDATA), Bytes.toBytes("Price:bid"),
        Bytes.toBytes(rate.getBid()));
    row.add(Bytes.toBytes(CFDATA), Bytes.toBytes("Price:ask"),
        Bytes.toBytes(rate.getAsk()));
    row.add(Bytes.toBytes(CFDATA), Bytes.toBytes("symbol"),
        Bytes.toBytes(rate.getSymbol()));
    table.put(row);
    
  }

  @SneakyThrows
  private void createTable(String tablename) {
    log.info("connected to hbase");
    HBaseAdmin admin = new HBaseAdmin(config);
    HTableDescriptor tdescriptor = new HTableDescriptor(TableName.valueOf(tablename));
    tdescriptor.addFamily(new HColumnDescriptor(CFDATA));
    tdescriptor.addFamily(new HColumnDescriptor(CFMETA));
    
    admin.createTable(tdescriptor);
    log.info("created table " + tablename);
    admin.close();
  }
  
}

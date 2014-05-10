/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.schema;

import io.fstream.core.model.Rate;

import java.text.SimpleDateFormat;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.joda.time.DateTime;
import lombok.*;



/**
 * 
 */
@Slf4j
public class FstreamHbaseSchema {

  private static String TABLENAME = "oanda";
  private static String CFDATA = "data";
  private static String CFMETA = "meta";
  private static String CTIME = "time";
  private static String CINST = "instrument";
  private static String CPRICE = "Price";
  private static String CBID = "bid";
  private static String COFFER = "offer";
  private static String ASSETSEGMENT = "segment";
  private static String EVENTTYPE = "event";
  private static String VOLUME = "volume";
  private static Configuration config;
  private static HTable table;

  private static final String[] sampledata = { "1399538895", "EURUSD", "1.0005", "1.0004", "QUOTE" };
  SimpleDateFormat timeformat = new SimpleDateFormat("dd MMM yyyy hh:mm:ss.S");

  public FstreamHbaseSchema() {
    // set
    config = HBaseConfiguration.create();
    initializeTable(TABLENAME);
  }

  public static void main(String[] args) {
    FstreamHbaseSchema schema = new FstreamHbaseSchema();
    //schema.createTable(TABLENAME);
    //schema.populateTable(TABLENAME);
  }
  
  @SneakyThrows
  private void initializeTable (String tablename) {
    HConnection connectionn = HConnectionManager.createConnection(config);
    log.info("connected to hbase");
    HBaseAdmin admin = new HBaseAdmin(config);
    if (admin.tableExists(tablename)) {
      if (admin.isTableDisabled(tablename)) { // assuming table schema is defined as per expected
        admin.enableTable(tablename);
      }
      log.info("table {} exists and is enabled",tablename);
    }
    else { // create table
      HTableDescriptor tdescriptor = new HTableDescriptor(tablename);
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

    // System.out.println(timeformat.format(time.getMillis()));
    // System.out.println(timeformat.format(timerounded.getMillis()));

    Put row = new Put(Bytes.toBytes(timerounded.getMillis() + sampledata[1]));
    row.add(Bytes.toBytes(CFDATA), Bytes.toBytes("Price:bid"),
        Bytes.toBytes(sampledata[2]));
    row.add(Bytes.toBytes(CFDATA), Bytes.toBytes("Price:ask"),
        Bytes.toBytes(sampledata[3]));
    table.put(row);
  }
  
  @SneakyThrows
  public void addRow(Rate rate) {
    //val timerounded = rate.getDateTime().hourOfDay().roundCeilingCopy();
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
    HConnection connectionn = HConnectionManager.createConnection(config);
    log.info("connected to hbase");
    HBaseAdmin admin = new HBaseAdmin(config);
    HTableDescriptor tdescriptor = new HTableDescriptor(tablename);
    tdescriptor.addFamily(new HColumnDescriptor(CFDATA));
    tdescriptor.addFamily(new HColumnDescriptor(CFMETA));
    admin.createTable(tdescriptor);
    log.info("created table " + tablename);
    // admin.addColumn(tablename, new HColumnDescriptor(CFDATA));
    admin.close();

  }
}

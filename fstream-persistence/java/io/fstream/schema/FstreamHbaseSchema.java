/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.schema;

import java.text.SimpleDateFormat;

import lombok.SneakyThrows;

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
import org.mortbay.log.Log;

/**
 * 
 */

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

  private static final String[] sampledata = { "1399538895", "EURUSD", "1.0005", "1.0004", "QUOTE" };
  SimpleDateFormat timeformat = new SimpleDateFormat("dd MMM yyyy hh:mm:ss.S");

  private FstreamHbaseSchema() {
    // set
    config = HBaseConfiguration.create();
  }

  public static void main(String[] args) {
    FstreamHbaseSchema schema = new FstreamHbaseSchema();
    // schema.createTable(TABLENAME);
    schema.populateTable(TABLENAME);
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
  private void createTable(String tablename) {
    HConnection connectionn = HConnectionManager.createConnection(config);
    Log.info("connected to hbase");
    HBaseAdmin admin = new HBaseAdmin(config);
    HTableDescriptor tdescriptor = new HTableDescriptor(tablename);
    tdescriptor.addFamily(new HColumnDescriptor(CFDATA));
    tdescriptor.addFamily(new HColumnDescriptor(CFMETA));
    admin.createTable(tdescriptor);
    Log.info("created table " + tablename);
    // admin.addColumn(tablename, new HColumnDescriptor(CFDATA));
    admin.close();

  }
}

/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persistence.hbase;

import io.fstream.core.model.Rate;

import java.text.SimpleDateFormat;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
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
public class Client {

  private static final String TABLENAME = "oanda";
  private static final String CFDATA = "data";
  private static final String CFMETA = "meta";
  private static Configuration config;
  private static HTable table;

  private static final String[] sampledata = { "1399538895", "EURUSD", "1.0005", "1.0004", "QUOTE" };
  SimpleDateFormat timeformat = new SimpleDateFormat("dd MMM yyyy hh:mm:ss.S");

  public Client() {
    // TODO for now the config file is set in the class path. need to set this programmatically
    config = HBaseConfiguration.create();
    config.set("hbase.rootdir","/Users/bdevani/data/hbase");
    config.set("base.zookeeper.property.dataDir","/Users/bdevani/data/hbase/zookeeper");
    initializeTable(TABLENAME);
  }

  public static void main(String[] args) {
    Client schema = new Client();
  }

  /**
   * Initializes table so it exists and ready to receive data
   * @param tablename
   */
  @SneakyThrows
  private void initializeTable(String tablename) {
    HBaseAdmin admin = new HBaseAdmin(config);
    if (admin.tableExists(tablename)) {
      //TODO assuming schema is created as per design. Schema validation would be nice
      if (admin.isTableDisabled(tablename)) { 
        admin.enableTable(tablename);
      }
      log.info("table {} exists and is enabled", tablename);
    }
    else { // create table
      HTableDescriptor tdescriptor = new HTableDescriptor(TableName.valueOf(tablename));
      tdescriptor.addFamily(new HColumnDescriptor(CFDATA));
      tdescriptor.addFamily(new HColumnDescriptor(CFMETA));
      admin.createTable(tdescriptor);
      log.info("table {} was created and enabled", tablename);
    }
    admin.close();
    table = new HTable(config, tablename);
  }

  @SneakyThrows
  public void addRow(Rate rate) {
    val timerounded = rate.getDateTime().hourOfDay().roundCeilingCopy();
    val row = new Put(Bytes.toBytes(timerounded.getMillis() + rate.getDateTime().getMillis() + rate.getSymbol()));
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

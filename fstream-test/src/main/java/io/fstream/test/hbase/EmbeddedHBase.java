/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.test.hbase;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.SneakyThrows;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseTestingUtility;

public class EmbeddedHBase {

  private HBaseTestingUtility utility;
  private Configuration conf;

  @SneakyThrows
  @PostConstruct
  public void launch() {
    utility = new HBaseTestingUtility();
    utility.startMiniCluster();
    conf = utility.getHBaseCluster().getConfiguration();
    conf.set("hbase.zookeeper.property.clientPort", "21818");
  }

  @PreDestroy
  @SneakyThrows
  public void shutdown() {
    utility.shutdownMiniCluster();
  }

}

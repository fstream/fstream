/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.test.hbase;

import lombok.val;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HBaseTestingUtility;

import com.google.common.util.concurrent.AbstractIdleService;

public class EmbeddedHBase extends AbstractIdleService {

  private HBaseTestingUtility utility;

  @Override
  protected void startUp() throws Exception {
    val config = HBaseConfiguration.create();
    config.set("test.hbase.zookeeper.property.clientPort", "21812");
    utility = new HBaseTestingUtility(config);

    utility.startMiniCluster();
  }

  @Override
  protected void shutDown() throws Exception {
    utility.shutdownMiniCluster();
  }

}

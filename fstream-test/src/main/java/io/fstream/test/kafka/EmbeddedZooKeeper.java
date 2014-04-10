/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.test.kafka;

import java.io.File;
import java.net.InetSocketAddress;

import lombok.RequiredArgsConstructor;
import lombok.val;

import org.apache.zookeeper.server.NIOServerCnxn;
import org.apache.zookeeper.server.NIOServerCnxn.Factory;
import org.apache.zookeeper.server.ZooKeeperServer;

import com.google.common.util.concurrent.AbstractIdleService;

@RequiredArgsConstructor
public class EmbeddedZooKeeper extends AbstractIdleService {

  private final File workDir;

  private Factory factory;

  @Override
  protected void startUp() throws Exception {
    val clientPort = 21818; // non-standard
    val numConnections = 5000;
    val tickTime = 2000;

    val server = new ZooKeeperServer(workDir, workDir, tickTime);
    val factory = new NIOServerCnxn.Factory(new InetSocketAddress(clientPort), numConnections);

    factory.startup(server);
  }

  @Override
  protected void shutDown() throws Exception {
    factory.shutdown();
  }

}

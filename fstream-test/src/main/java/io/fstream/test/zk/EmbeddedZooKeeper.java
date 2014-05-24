/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.test.zk;

import java.io.File;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.curator.test.TestingServer;

import com.google.common.util.concurrent.AbstractIdleService;

@Slf4j
@RequiredArgsConstructor
public class EmbeddedZooKeeper extends AbstractIdleService {

  /**
   * Configuration.
   */
  @NonNull
  private final String zkConnect;
  @NonNull
  private final File tempDir;

  /**
   * State.
   */
  private TestingServer server;

  @Override
  protected void startUp() throws Exception {
    val clientPort = getZkClientPort();

    log.info("Starting testing server...");
    server = new TestingServer(clientPort, tempDir);
    log.info("Finished starting testing server");
  }

  @Override
  protected void shutDown() throws Exception {
    log.info("Stopping testing server...");
    server.stop();
    log.info("Finished topting testing server");
  }

  private int getZkClientPort() {
    log.info("Parsing client port with zkConnect = '{}'", zkConnect);
    return Integer.valueOf(zkConnect.split(":")[1]);
  }

}

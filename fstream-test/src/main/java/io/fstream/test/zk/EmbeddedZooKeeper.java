/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.test.zk;

import static io.fstream.core.util.ZooKeepers.parseZkPort;

import java.io.File;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.curator.test.TestingServer;

@Slf4j
@RequiredArgsConstructor
public class EmbeddedZooKeeper {

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

  public void startUp() throws Exception {
    log.info("Parsing client port with zkConnect = '{}'", zkConnect);
    val clientPort = parseZkPort(zkConnect);

    log.info("Starting testing server using tempDir '{}'...", tempDir.getAbsolutePath());
    server = new TestingServer(clientPort, tempDir);
    log.info("Finished starting testing server");
  }

  public void shutDown() throws Exception {
    log.info("Stopping testing server...");
    server.stop();
    log.info("Finished topting testing server");
  }

}

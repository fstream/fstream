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

import org.apache.curator.test.TestingServer;

import com.google.common.util.concurrent.AbstractIdleService;

@RequiredArgsConstructor
public class EmbeddedZooKeeper extends AbstractIdleService {

  @NonNull
  private final File snapDir;
  @NonNull
  private final File logDir;

  private TestingServer server;

  @Override
  protected void startUp() throws Exception {
    val clientPort = 21818; // non-standard
    
    server = new TestingServer(clientPort, snapDir);
  }

  @Override
  protected void shutDown() throws Exception {
    server.stop();
  }

}

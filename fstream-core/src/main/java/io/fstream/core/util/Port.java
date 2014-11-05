/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.core.util;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Port {

  @NonNull
  private final String host;
  private final int port;

  public void waitFor(long timeValue, TimeUnit timeUnit) throws IOException, InterruptedException {
    val address = new InetSocketAddress(host, port);
    val duration = timeUnit.toMillis(timeValue);
    val threshold = System.currentTimeMillis() + duration;

    try (val socket = new Socket()) {
      log.info("Waiting up to {} {} for {} to become available...", timeValue, timeUnit, address);
      while (System.currentTimeMillis() < threshold) {
        try {
          socket.connect(address, (int) duration);

          break;
        } catch (SocketException e) {
          log.info("Waiting...");
          Thread.sleep(SECONDS.toMillis(10));
        }
      }
    }
  }

}

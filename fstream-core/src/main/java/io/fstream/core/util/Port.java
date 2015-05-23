/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.core.util;

import static com.google.common.base.Stopwatch.createStarted;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Port {

  @NonNull
  private final String host;
  private final int port;

  @SneakyThrows
  public void waitFor(long timeValue, TimeUnit timeUnit) {
    val address = new InetSocketAddress(host, port);
    val duration = timeUnit.toMillis(timeValue);
    val threshold = System.currentTimeMillis() + duration;

    log.info("Waiting up to {} {} for {}:{} to become available...", timeValue, timeUnit, host, port);
    val watch = createStarted();
    while (System.currentTimeMillis() < threshold) {
      try (val socket = new Socket()) {
        socket.connect(address, (int) duration);

        log.info("Port available!");
        return;
      } catch (SocketException e) {
        log.info("Waiting ({})...", watch);
        Thread.sleep(SECONDS.toMillis(5));
      }
    }

    log.warn("After {} {} portal not available for {}:{}!", timeValue, timeUnit, host, port);
  }

}

/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.core.util;

import static java.util.stream.Collectors.toList;

import java.util.List;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

import com.google.common.base.Splitter;
import com.google.common.net.HostAndPort;

@UtilityClass
public class ZooKeepers {

  /**
   * Constants.
   */
  private static final Splitter CONNECT_SPLITTER = Splitter.on(',').trimResults();

  public List<HostAndPort> parseZkConnect(@NonNull String zkConnect) {
    val tokens = CONNECT_SPLITTER.splitToList(zkConnect);

    return tokens.stream().map(HostAndPort::fromString).collect(toList());
  }

  public List<String> parseZkServers(@NonNull String zkConnect) {
    return parseZkConnect(zkConnect).stream().map(HostAndPort::getHostText).collect(toList());
  }

  public int parseZkPort(@NonNull String zkConnect) {
    return parseZkConnect(zkConnect).get(0).getPort();
  }

}

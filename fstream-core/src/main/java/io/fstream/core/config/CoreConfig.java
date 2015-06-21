/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.core.config;

import static java.util.concurrent.TimeUnit.MINUTES;
import io.fstream.core.model.state.State;
import io.fstream.core.service.StateService;
import io.fstream.core.util.Port;
import io.fstream.core.util.ZooKeepers;
import lombok.val;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class CoreConfig {

  @Autowired
  protected State state;

  @Autowired
  protected StateService stateService;

  @Bean
  @Profile("disabled")
  public Port zkPort(@Value("${zk.connect}") String zkConnect) {
    val hosts = ZooKeepers.parseZkConnect(zkConnect);
    val host = hosts.get(0); // Arbitrary

    val zkPort = new Port(host.getHostText(), host.getPort());
    zkPort.waitFor(1L, MINUTES);

    return zkPort;
  }

}

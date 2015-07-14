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

import java.util.List;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

@Slf4j
public class CoreConfig {

  /**
   * Configuration.
   */
  @Value("#{environment.getActiveProfiles()}")
  List<String> activeProfiles;

  /**
   * Dependencies.
   */
  @Autowired
  protected State state;
  @Autowired
  protected StateService stateService;

  @Bean
  @ConditionalOnExpression("false")
  public Port zkPort(@Value("${zk.connect}") String zkConnect) {
    val hosts = ZooKeepers.parseZkConnect(zkConnect);
    val host = hosts.get(0); // Arbitrary

    val zkPort = new Port(host.getHostText(), host.getPort());
    zkPort.waitFor(1L, MINUTES);

    return zkPort;
  }

  @EventListener
  public void start(ApplicationReadyEvent ready) {
    log.info("");
    log.info("Active profiles: {}", activeProfiles);
    log.info("");
  }

}

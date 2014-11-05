/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.config;

import static java.util.concurrent.TimeUnit.MINUTES;
import io.fstream.compute.storm.DistributedStormJobExecutor;
import io.fstream.compute.storm.LocalStormJobExecutor;
import io.fstream.compute.storm.StormJobExecutor;
import io.fstream.core.config.CoreConfig;
import io.fstream.core.util.Port;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application level configuration.
 */
@Slf4j
@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties
public class ComputeConfig extends CoreConfig {

  @Bean
  public StormJobExecutor stormJobExecutor(@Value("${zk.host}") String host, @Value("${zk.port}") int port,
      @Value("${storm.local}") boolean local) throws IOException, InterruptedException {
    new Port(host, port).waitFor(1L, MINUTES);

    if (local) {
      log.info("Creating local storm job executor...");
      return new LocalStormJobExecutor();
    } else {
      log.info("Creating distributed job storm executor...");
      return new DistributedStormJobExecutor();
    }
  }

}

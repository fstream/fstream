/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.config;

import static java.util.concurrent.TimeUnit.MINUTES;
import io.fstream.core.config.CoreConfig;
import io.fstream.core.util.Port;

import javax.annotation.PostConstruct;

import lombok.SneakyThrows;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties
public class RatesConfig extends CoreConfig {

  @Value("${zk.host}")
  String host;
  @Value("${zk.port}")
  int port;

  @PostConstruct
  @SneakyThrows
  public void init() {
    new Port(host, port).waitFor(1L, MINUTES);
  }

}

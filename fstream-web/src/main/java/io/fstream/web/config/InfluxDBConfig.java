/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.web.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class InfluxDBConfig {

  @Value("${influxdb.url}")
  private String url;
  @Value("${influxdb.username}")
  private String username;
  @Value("${influxdb.password}")
  private String password;

  @Bean
  @SneakyThrows
  public InfluxDB influxDb() {
    log.info("Connection to '{}' as user '{}'", url, username);
    return InfluxDBFactory.connect(url, username, password);
  }

}

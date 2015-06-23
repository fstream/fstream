/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.core.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * InfluxDB specific configuration properties.
 */
@Data
@Component
@ConfigurationProperties("influxdb")
public class InfluxDBProperties {

  private String url;
  private String username;
  private String password;

}

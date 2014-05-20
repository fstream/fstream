/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persistence.config;

import static org.apache.hadoop.hbase.HConstants.ZOOKEEPER_CLIENT_PORT;
import lombok.SneakyThrows;
import lombok.val;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HBaseConfig {

  @Value("${zk.port}")
  private String zkConnect;

  @Bean
  public org.apache.hadoop.conf.Configuration config() {
    val config = HBaseConfiguration.create();
    config.set(ZOOKEEPER_CLIENT_PORT, zkConnect);

    return config;
  }

  @Bean
  @SneakyThrows
  public HBaseAdmin hbaseAdmin() {
    return new HBaseAdmin(config());
  }

}

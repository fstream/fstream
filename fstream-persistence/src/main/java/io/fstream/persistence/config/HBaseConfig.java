/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persistence.config;

import io.fstream.persistence.hbase.Client;
import lombok.SneakyThrows;
import lombok.val;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HBaseConfig {

  @Bean
  public Client client() {
    return new Client();
  }
  
  @Bean
  public org.apache.hadoop.conf.Configuration config() {
    val config = HBaseConfiguration.create();
    config.set("hbase.rootdir", "/var/lib/hbase/data/hbase");
    config.set("base.zookeeper.property.dataDir", "/var/lib/hbase/data/zookeeper");
    
    return config;
  }
  
  @Bean
  @SneakyThrows
  public HBaseAdmin hbaseAdmin() {
    return new HBaseAdmin(config());
  }
  
}

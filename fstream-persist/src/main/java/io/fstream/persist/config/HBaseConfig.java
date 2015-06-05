/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist.config;

import static io.fstream.core.util.ZooKeepers.parseZkConnect;
import static java.util.stream.Collectors.joining;
import static org.apache.hadoop.hbase.HConstants.ZOOKEEPER_CLIENT_PORT;
import static org.apache.hadoop.hbase.HConstants.ZOOKEEPER_QUORUM;
import lombok.SneakyThrows;
import lombok.val;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.google.common.net.HostAndPort;

@Profile("hbase")
@Configuration
public class HBaseConfig {

  @Value("${zk.connect}")
  private String zkConnect;

  @Bean
  public org.apache.hadoop.conf.Configuration config() {
    val hosts = parseZkConnect(zkConnect);
    val zkServers = hosts.stream().map(HostAndPort::getHostText).collect(joining(","));
    val zkPort = Integer.toString(hosts.get(0).getPort()); // Assumes all are the same

    val config = HBaseConfiguration.create();
    config.set(ZOOKEEPER_QUORUM, zkServers);
    config.set(ZOOKEEPER_CLIENT_PORT, zkPort);

    return config;
  }

  @Bean
  @SneakyThrows
  public Connection hbaseConnection() {
    return ConnectionFactory.createConnection(config());
  }

  @Bean
  @SneakyThrows
  public Admin hbaseAdmin() {
    return hbaseConnection().getAdmin();
  }

}

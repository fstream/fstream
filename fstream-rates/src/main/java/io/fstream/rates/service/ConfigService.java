/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.service;

import static com.google.common.io.Closeables.close;
import io.fstream.rates.config.RatesProperties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class ConfigService {

  /**
   * Constants.
   */
  private static final String PATH = "/fstream/config";
  private static final ObjectMapper MAPPER = new ObjectMapper();

  /**
   * Configuration.
   */
  @Value("${zk.connect}")
  private String zkConnect;
  @Autowired
  private RatesProperties properties;

  /**
   * State.
   */
  private CuratorFramework client;
  private PathChildrenCache cache;

  @SneakyThrows
  @PostConstruct
  public void initialize() {
    this.client = CuratorFrameworkFactory.newClient(zkConnect, new ExponentialBackoffRetry(1000, 3));
    this.cache = new PathChildrenCache(client, PATH, true);

    log.info("Starting client...");
    client.start();
    log.info("Started client.");

    log.info("Starting cache...");
    cache.start();
    log.info("Started cache.");

    // Perform update
    write();
  }

  @SneakyThrows
  @PreDestroy
  public void destroy() {
    log.info("Closing cache...");
    close(cache, true);
    log.info("Closed cache.");

    log.info("Closing client...");
    close(client, true);
    log.info("Closed client.");
  }

  @SneakyThrows
  public void write() {
    val bytes = serialize();

    val exists = client.checkExists().forPath(PATH) != null;
    if (exists) {
      log.info("Updating existing configuration at path '{}'...", PATH);
      client.setData().forPath(PATH, bytes);
      log.info("Updated configuration at path '{}'.", PATH);
    } else {
      log.info("Creating configuration at path '{}'...", PATH);
      client.create().creatingParentsIfNeeded().forPath(PATH, bytes);
      log.info("Created configuration at path '{}'.", PATH);
    }
  }

  @SneakyThrows
  public byte[] serialize() {
    return MAPPER.writeValueAsBytes(properties.getSymbols());
  }

}

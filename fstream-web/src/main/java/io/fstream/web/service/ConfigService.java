/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.web.service;

import static com.google.common.io.Closeables.close;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
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
  public List<String> read() {
    log.info("Getting configuration at path '{}'...", PATH);
    val bytes = client.getData().forPath(PATH);
    log.info("Got configuration at path '{}'.", PATH);
   
    return deserialize(bytes);
  }

  @SneakyThrows
  private List<String> deserialize(byte[] bytes) {
    return MAPPER.readValue(bytes, new TypeReference<ArrayList<String>>() {});
  }

}

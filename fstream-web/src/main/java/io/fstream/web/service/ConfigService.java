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

import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
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
  private static final TypeReference<ArrayList<String>> STRING_LIST = new TypeReference<ArrayList<String>>() {};

  /**
   * Configuration.
   */
  @Value("${zk.connect}")
  private String zkConnect;

  /**
   * Dependencies.
   */
  @Setter
  @Autowired
  protected SimpMessagingTemplate template;

  /**
   * State.
   */
  private CuratorFramework client;
  private PathChildrenCache cache;

  @PostConstruct
  @SneakyThrows
  public void initialize() {
    this.client = CuratorFrameworkFactory.newClient(zkConnect, new ExponentialBackoffRetry(1000, 3));
    this.cache = new PathChildrenCache(client, PATH, true);

    log.info("Starting client...");
    client.start();
    log.info("Started client.");

    log.info("Starting cache...");
    cache.start();
    log.info("Started cache.");

    cache.getListenable().addListener(new PathChildrenCacheListener() {

      @Override
      public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
        // TODO: Decouple websocket communication from service via events
        log.info("Configuration updated: {}", event);
        template.send("/topic/commands", convert(event.getData().getData()));
      }

    });
  }

  @PreDestroy
  @SneakyThrows
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
    byte[] bytes = client.getData().forPath(PATH);
    log.info("Got configuration at path '{}'.", PATH);

    return deserialize(bytes);
  }

  @SneakyThrows
  private List<String> deserialize(byte[] bytes) {
    return MAPPER.readValue(bytes, STRING_LIST);
  }

  private static Message<byte[]> convert(final byte[] message) {
    return MessageBuilder.withPayload(message).build();
  }

}

/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.core.service;

import static com.google.common.io.Closeables.close;
import io.fstream.core.model.state.State;
import io.fstream.core.model.state.StateListener;
import io.fstream.core.util.Codec;

import javax.annotation.PreDestroy;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StateService {

  /**
   * Constants.
   */
  private static final String PARENT_PATH = "/fstream";
  private static final String STATE_PATH = PARENT_PATH + "/state";

  /**
   * state.
   */
  @Value("${zk.connect}")
  private String zkConnect;

  /**
   * State.
   */
  private CuratorFramework client;
  private PathChildrenCache cache;

  @SneakyThrows
  public void initialize() {
    log.info("Starting client...");
    this.client = createClient();
    client.start();
    log.info("Started client.");

    log.info("Starting cache...");
    this.cache = createCache();
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
  public State getState() {
    log.info("Getting state at path '{}'...", STATE_PATH);
    byte[] bytes = client.getData().forPath(STATE_PATH);
    log.info("Got state at path '{}'.", STATE_PATH);

    return deserialize(bytes);
  }

  @SneakyThrows
  public void setState(State state) {
    byte[] bytes = serialize(state);

    val exists = client.checkExists().forPath(STATE_PATH) != null;
    if (exists) {
      log.info("Updating existing state at path '{}'...", STATE_PATH);
      client.setData().forPath(STATE_PATH, bytes);
      log.info("Updated state at path '{}'.", STATE_PATH);
    } else {
      log.info("Creating state at path '{}'...", STATE_PATH);
      client.create().creatingParentsIfNeeded().forPath(STATE_PATH, bytes);
      log.info("Created state at path '{}'.", STATE_PATH);
    }
  }

  public void addListener(final StateListener listener) {
    cache.getListenable().addListener(new PathChildrenCacheListener() {

      @Override
      public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
        val state = deserialize(event.getData().getData());

        log.info("Cache updated: {}", state);
        listener.onUpdate(state);
      }

    });
  }

  private CuratorFramework createClient() {
    val retryPolicy = new ExponentialBackoffRetry(1000, 3);

    return CuratorFrameworkFactory.newClient(zkConnect, retryPolicy);
  }

  private PathChildrenCache createCache() {
    // TODO: Use version number when performing updates
    // TODO: This should really be watching STATE_PATH but it doesn't seem to trigger
    val cacheData = true;
    return new PathChildrenCache(client, PARENT_PATH, cacheData);
  }

  @SneakyThrows
  private byte[] serialize(State state) {
    return Codec.encodeBytes(state);
  }

  @SneakyThrows
  private State deserialize(byte[] bytes) {
    return Codec.decodeBytes(bytes, State.class);
  }

}

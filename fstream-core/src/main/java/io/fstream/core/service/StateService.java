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

@Slf4j
// @Service
public class StateService {

  /**
   * Constants.
   */
  private static final String PATH = "/fstream/state";

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
  public State read() {
    log.info("Getting state at path '{}'...", PATH);
    byte[] bytes = client.getData().forPath(PATH);
    log.info("Got state at path '{}'.", PATH);

    return deserialize(bytes);
  }

  @SneakyThrows
  public void write(State state) {
    byte[] bytes = serialize(state);

    val exists = client.checkExists().forPath(PATH) != null;
    if (exists) {
      log.info("Updating existing state at path '{}'...", PATH);
      client.setData().forPath(PATH, bytes);
      log.info("Updated state at path '{}'.", PATH);
    } else {
      log.info("Creating state at path '{}'...", PATH);
      client.create().creatingParentsIfNeeded().forPath(PATH, bytes);
      log.info("Created state at path '{}'.", PATH);
    }
  }

  public void register(final StateListener listener) {
    cache.getListenable().addListener(new PathChildrenCacheListener() {

      @Override
      public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
        val state = deserialize(event.getData().getData());

        listener.onUpdate(state);
      }

    });
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

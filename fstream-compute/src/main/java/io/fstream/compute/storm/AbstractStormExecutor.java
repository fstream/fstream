/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.storm;

import io.fstream.compute.config.KafkaProperties;
import io.fstream.compute.config.StormProperties;
import io.fstream.core.model.state.State;

import java.util.UUID;

import lombok.Setter;
import lombok.val;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@Setter
public abstract class AbstractStormExecutor implements StormExecutor {

  /**
   * Configuration.
   */
  @Value("${zk.connect}")
  private String zkConnect;
  @Autowired
  private StormProperties stormProperties;
  @Autowired
  private KafkaProperties kafkaProperties;

  @Override
  public void execute(State state) {
    val job = createJob(state);

    executeJob(job);
  }

  /**
   * Template method.
   */
  protected abstract void executeJob(StormJob job);

  protected StormJob createJob(State state) {
    val id = UUID.randomUUID().toString();

    return new StormJob(zkConnect, kafkaProperties, id, state);
  }

  protected static void onShutdown(Runnable runnable) {
    Runtime.getRuntime().addShutdownHook(new Thread(runnable));
  }

}

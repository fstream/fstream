/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.service;

import io.fstream.compute.storm.StormExecutor;
import io.fstream.core.model.state.State;
import io.fstream.core.model.state.StateListener;
import io.fstream.core.service.StateService;

import javax.annotation.PostConstruct;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Compute job submission entry point.
 */
@Slf4j
@Service
public class ComputeService implements StateListener {

  /**
   * Dependencies.
   */
  @Autowired
  private StateService stateService;
  @Autowired
  private StormExecutor stormExecutor;

  @PostConstruct
  @SneakyThrows
  public void init() {
    log.info("Registering for state updates...");
    stateService.initialize();
    stateService.register(this);

    log.info("Submitting storm topologies...");
    val state = stateService.read();
    submit(state);
  }

  @Override
  @SneakyThrows
  public void onUpdate(State state) {
    log.info("Submitting storm topology...");
    submit(state);
  }

  private void submit(State state) throws Exception {
    stormExecutor.execute(state);
  }

}

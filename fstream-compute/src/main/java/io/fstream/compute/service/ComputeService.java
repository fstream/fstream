/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.service;

import io.fstream.compute.storm.StormExecutorService;
import io.fstream.core.model.state.State;
import io.fstream.core.model.state.StateListener;
import io.fstream.core.service.StateService;

import javax.annotation.PostConstruct;

import lombok.SneakyThrows;
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
  private State state;
  @Autowired
  private StateService stateService;
  @Autowired
  private StormExecutorService executorService;

  @PostConstruct
  @SneakyThrows
  public void init() {
    log.info("Registering for state updates...");
    // stateService.initialize();
    // stateService.register(this);

    log.info("Submitting storm topologies...");
    executorService.execute(state);
  }

  @Override
  @SneakyThrows
  public void onUpdate(State state) {
    log.info("Submitting storm topology...");
    executorService.execute(state);
  }

}

/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.service;

import static org.assertj.core.util.Maps.newConcurrentHashMap;
import io.fstream.compute.storm.StormExecutor;
import io.fstream.core.model.definition.Alert;
import io.fstream.core.model.definition.Metric;
import io.fstream.core.model.state.State;
import io.fstream.core.model.state.StateListener;
import io.fstream.core.service.StateService;

import java.util.Map;

import javax.annotation.PostConstruct;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;

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

  private final Map<Integer, Alert> alerts = newConcurrentHashMap();
  private final Map<Integer, Metric> metrics = newConcurrentHashMap();

  @PostConstruct
  @SneakyThrows
  public void initialize() {
    log.info("Registering for state updates...");
    stateService.initialize();
    stateService.addListener(this);

    // Bootstrap initial job
    val state = stateService.getState();
    onUpdate(state);
  }

  @Override
  @SneakyThrows
  public void onUpdate(State nextState) {
    // TODO: Support removal of definitions
    log.info("Updating state...");
    for (val alert : nextState.getAlerts()) {
      if (alerts.containsKey(alert.getId())) {
        // Skip
        continue;
      }

      val command = new State();
      command.setSymbols(nextState.getSymbols());
      command.setStatements(nextState.getStatements());
      command.setAlerts(ImmutableList.of(alert));

      log.info("Submitting storm topology: {}...", command);
      stormExecutor.execute(command);

      alerts.put(alert.getId(), alert);
    }

    for (val metric : nextState.getMetrics()) {
      if (metrics.containsKey(metric.getId())) {
        // Skip
        continue;
      }

      val command = new State();
      command.setSymbols(nextState.getSymbols());
      command.setStatements(nextState.getStatements());
      command.setMetrics(ImmutableList.of(metric));

      log.info("Submitting storm topology: {}...", command);
      stormExecutor.execute(command);

      metrics.put(metric.getId(), metric);
    }
  }

}

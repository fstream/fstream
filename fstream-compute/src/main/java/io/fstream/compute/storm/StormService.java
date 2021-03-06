/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.storm;

import static com.google.common.base.Strings.repeat;
import static com.google.common.collect.Maps.newConcurrentMap;
import io.fstream.core.model.definition.Alert;
import io.fstream.core.model.definition.Metric;
import io.fstream.core.model.state.State;
import io.fstream.core.model.state.StateListener;
import io.fstream.core.service.StateService;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.google.common.collect.Ordering;

/**
 * Compute job submission entry point.
 */
@Slf4j
@Service
@Profile("storm")
public class StormService implements StateListener {

  /**
   * Dependencies.
   */
  @Autowired
  private StateService stateService;
  @Autowired
  private StormJobFactory jobFactory;
  @Autowired
  private StormJobExecutor jobExecutor;

  /**
   * State.
   */
  private final Map<Integer, Alert> alerts = newConcurrentMap();
  private final Map<Integer, Metric> metrics = newConcurrentMap();

  @PostConstruct
  @SneakyThrows
  public void initialize() {
    log.info("Registering for state updates...");
    stateService.initialize();
    stateService.addListener(this);
  }

  @Override
  @SneakyThrows
  public void onUpdate(@NonNull State nextState) {
    // TODO: Support removal of definitions
    log.info("{}", repeat("-", 100));
    log.info("Updating state...");
    log.info("{}", repeat("-", 100));
    val symbols = nextState.getSymbols();
    val common = nextState.getStatements();

    log.info("Submitting alerts...");
    submitAlerts(nextState.getAlerts(), symbols, common);

    log.info("Submitting metrics...");
    submitMetrics(nextState.getMetrics(), symbols, common);
  }

  private void submitAlerts(List<Alert> alerts, List<String> symbols, List<String> common) {
    for (val alert : alerts) {
      if (isAlertExecuting(alert)) {
        // TODO: kill and resubmit
        continue;
      }

      if (alert.getId() == 0) {
        val id = nextAlertId();
        alert.setId(id);
      }

      submitAlert(alert, symbols, common);
    }
  }

  private void submitAlert(Alert alert, List<String> symbols, List<String> common) {
    val alertJob = jobFactory.createAlertJob(alert, symbols, common);

    log.info("Submitting storm alert topology: '{}'...", alert.getName());
    jobExecutor.execute(alertJob);

    // TODO: Need to store job as well. Use Table
    alerts.put(alert.getId(), alert);
  }

  private void submitMetrics(List<Metric> metrics, List<String> symbols, List<String> common) {
    for (val metric : metrics) {
      if (isMetricExecuting(metric)) {
        // TODO: kill and resubmit
        continue;
      }

      if (metric.getId() == 0) {
        val id = nextMetricId();
        metric.setId(id);
      }

      submitMetric(metric, symbols, common);
    }
  }

  private void submitMetric(Metric metric, List<String> symbols, List<String> common) {
    val metricJob = jobFactory.createMetricJob(metric, symbols, common);

    log.info("Submitting storm metric topology: '{}'...", metric.getName());
    jobExecutor.execute(metricJob);

    // TODO: Need to store job as well. Use Table
    metrics.put(metric.getId(), metric);
  }

  private int nextAlertId() {
    return Ordering.natural().max(this.alerts.keySet()) + 1;
  }

  private int nextMetricId() {
    return Ordering.natural().max(this.metrics.keySet()) + 1;
  }

  private boolean isAlertExecuting(Alert alert) {
    return alerts.containsKey(alert.getId());
  }

  private boolean isMetricExecuting(Metric metric) {
    return metrics.containsKey(metric.getId());
  }

}

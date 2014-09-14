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
import io.fstream.core.model.definition.Alert;
import io.fstream.core.model.definition.Metric;
import io.fstream.core.model.state.State;

import java.util.List;
import java.util.UUID;

import lombok.NonNull;
import lombok.val;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

/**
 * Factory responsible for creating fully initialized {@link StormJob} instances.
 */
@Component
public class StormJobFactory {

  /**
   * Configuration.
   */
  @Value("${zk.connect}")
  private String zkConnect;
  @Autowired
  private KafkaProperties kafkaProperties;

  public StormJob createJob(@NonNull State state) {
    val id = UUID.randomUUID().toString();
  
    return new StormJob(zkConnect, kafkaProperties, id, state);
  }

  public StormJob createAlertJob(@NonNull Alert alert, List<String> symbols, List<String> common) {
    // Singleton alert
    val state = new State();
    state.setAlerts(ImmutableList.of(alert));
    state.setSymbols(symbols);
    state.setStatements(common);

    return createJob(state);
  }

  public StormJob createMetricJob(@NonNull Metric metric, List<String> symbols, List<String> common) {
    // Singleton metric
    val state = new State();
    state.setMetrics(ImmutableList.of(metric));
    state.setSymbols(symbols);
    state.setStatements(common);

    return createJob(state);
  }

}

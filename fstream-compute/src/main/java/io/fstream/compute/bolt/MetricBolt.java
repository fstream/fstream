/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.bolt;

import io.fstream.core.model.definition.Metric;
import io.fstream.core.model.event.Event;
import io.fstream.core.model.event.MetricEvent;
import io.fstream.core.util.Codec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;

import com.espertech.esper.client.EPAdministrator;
import com.fasterxml.jackson.core.type.TypeReference;

@Slf4j
public class MetricBolt extends EsperBolt {

  /**
   * Configuration keys.
   */
  public static final String METRICS_CONFIG_KEY = Metric.class.getName();

  @Override
  protected void createStatements(Map<?, ?> conf, EPAdministrator admin) {
    val metrics = getMetrics(conf);
    for (val metric : metrics) {
      log.info("Registering metric: {}", metric);
      val statement = admin.createEPL(metric.getStatement(), metric);

      statement.addListener(this);
    }
  }

  @Override
  protected Event createEvent(int id, Object data) {
    return new MetricEvent(new DateTime(), id, data);
  }

  @SneakyThrows
  private static List<Metric> getMetrics(Map<?, ?> conf) {
    val value = (String) conf.get(METRICS_CONFIG_KEY);

    return Codec.decodeText(value, new TypeReference<ArrayList<Metric>>() {});
  }

}
/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.bolt;

import io.fstream.core.model.definition.Alert;
import io.fstream.core.model.definition.Metric;
import io.fstream.core.model.event.AlertEvent;
import io.fstream.core.model.event.TickEvent;
import io.fstream.core.util.Codec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.fasterxml.jackson.core.type.TypeReference;

@Slf4j
public class ComputeBolt extends BaseRichBolt implements UpdateListener {

  /**
   * Constants.
   */
  private static final String KAFKA_TOPIC_KEY = "1";

  /**
   * Configuration keys.
   */
  public static final String ALERTS_CONFIG_KEY = "io.fstream.alerts";
  public static final String METRICS_CONFIG_KEY = "io.fstream.metrics";

  /**
   * Esper.
   */
  private transient EPServiceProvider esperSink;
  private transient EPRuntime runtime;
  private transient EPAdministrator admin;

  /**
   * Storm.
   */
  private transient OutputCollector collector;

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields(KafkaBolt.BOLT_KEY, KafkaBolt.BOLT_MESSAGE));
  }

  @Override
  public void prepare(@SuppressWarnings("rawtypes") Map conf, TopologyContext context, OutputCollector collector) {
    log.info("Preparing...");
    val configuration = new Configuration();
    configuration.addEventType("Rate", TickEvent.class.getName());

    this.collector = collector;
    this.esperSink = EPServiceProviderManager.getProvider(this.toString(), configuration);
    this.esperSink.initialize();
    this.runtime = esperSink.getEPRuntime();
    this.admin = esperSink.getEPAdministrator();

    val alerts = getAlerts(conf);
    for (val alert : alerts) {
      log.info("Registering alert: {}", alert);
      val statement = admin.createEPL(alert.getStatement());

      statement.addListener(this);
    }

    val metrics = getMetrics(conf);
    for (val metric : metrics) {
      log.info("Registering metric: {}", metric);
      val statement = admin.createEPL(metric.getStatement());

      statement.addListener(this);
    }

    log.info("Finished preparing.");
  }

  @Override
  @SneakyThrows
  public void execute(Tuple tuple) {
    val value = (String) tuple.getValue(0);
    val event = Codec.decodeText(value, TickEvent.class);

    runtime.sendEvent(event);

    collector.ack(tuple);
  }

  @Override
  @SneakyThrows
  public void update(EventBean[] newEvents, EventBean[] oldEvents) {
    if (newEvents != null) {
      for (val newEvent : newEvents) {
        val data = newEvent.getUnderlying();
        val event = new AlertEvent(new DateTime(), data);
        val value = Codec.encodeText(event);

        collector.emit(new Values(KAFKA_TOPIC_KEY, value));
      }
    }
  }

  @Override
  public void cleanup() {
    if (esperSink != null) {
      esperSink.destroy();
    }
  }

  @SneakyThrows
  private List<Alert> getAlerts(Map<?, ?> conf) {
    val value = (String) conf.get(ALERTS_CONFIG_KEY);

    return Codec.decodeText(value, new TypeReference<ArrayList<Alert>>() {});
  }

  @SneakyThrows
  private List<Metric> getMetrics(Map<?, ?> conf) {
    val value = (String) conf.get(METRICS_CONFIG_KEY);

    return Codec.decodeText(value, new TypeReference<ArrayList<Metric>>() {});
  }

}
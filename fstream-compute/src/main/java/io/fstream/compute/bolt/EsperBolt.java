/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.bolt;

import io.fstream.core.model.event.Event;
import io.fstream.core.model.event.TickEvent;
import io.fstream.core.util.Codec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
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
public abstract class EsperBolt extends BaseRichBolt implements UpdateListener {

  /**
   * Constants.
   */
  private static final String KAFKA_TOPIC_KEY = "1";

  /**
   * Configuration keys.
   */
  public static final String STATEMENTS_CONFIG_KEY = "io.fstream.alerts";

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

    log.info("Creating common statements...");
    for (val statement : getStatements(conf)) {
      log.info("Registering statement: {}", statement);
      val epl = admin.createEPL(statement);

      epl.addListener(this);
    }
    log.info("Finished creating common statements.");

    // Delegate to child
    log.info("Creating '{}' statements...", this.getClass().getSimpleName());
    createStatements(conf, admin);
    log.info("Finished creating '{}' statements.", this.getClass().getSimpleName());
  }

  /**
   * Template methods.
   */
  protected abstract void createStatements(Map<?, ?> conf, EPAdministrator admin);

  /**
   * Template methods.
   */
  protected abstract Event createEvent(Object data);

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
        val event = createEvent(data);
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
  private static List<String> getStatements(Map<?, ?> conf) {
    val value = (String) conf.get(STATEMENTS_CONFIG_KEY);

    return Codec.decodeText(value, new TypeReference<ArrayList<String>>() {});
  }

}
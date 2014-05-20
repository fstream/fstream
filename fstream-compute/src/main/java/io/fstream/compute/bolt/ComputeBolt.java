/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.bolt;

import io.fstream.core.model.Rate;

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
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
public class ComputeBolt extends BaseRichBolt implements UpdateListener {

  public static final String EPL = "epl";

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private transient EPServiceProvider esperSink;
  private transient EPRuntime runtime;
  private transient EPAdministrator admin;
  private transient OutputCollector collector;

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields(KafkaBolt.BOLT_KEY, KafkaBolt.BOLT_MESSAGE));
  }

  @Override
  public void prepare(@SuppressWarnings("rawtypes") Map conf, TopologyContext context, OutputCollector collector) {
    log.info("Preparing...");
    val configuration = new Configuration();
    configuration.addEventType("Rate", Rate.class.getName());

    this.collector = collector;
    this.esperSink = EPServiceProviderManager.getProvider(this.toString(), configuration);
    this.esperSink.initialize();
    this.runtime = esperSink.getEPRuntime();
    this.admin = esperSink.getEPAdministrator();

    val values = getEplValues(conf);
    for (val epl : values) {
      log.info("Registering statement: {}", epl);
      val statement = admin.createEPL(epl);

      statement.addListener(this);
    }

    log.info("Finished preparing.");
  }

  @Override
  @SneakyThrows
  public void execute(Tuple tuple) {
    val content = (String) tuple.getValue(0);
    val rate = MAPPER.readValue(content, Rate.class);

    runtime.sendEvent(rate);

    collector.ack(tuple);
  }

  @Override
  @SneakyThrows
  public void update(EventBean[] newEvents, EventBean[] oldEvents) {
    if (newEvents != null) {
      for (val newEvent : newEvents) {
        val alert = newEvent.getUnderlying();
        val content = MAPPER.writeValueAsString(alert);
        collector.emit(new Values("1", content));
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
  private List<String> getEplValues(Map<?, ?> conf) {
    val epl = (String) conf.get(EPL);
    
    return MAPPER.readValue(epl, new TypeReference<ArrayList<String>>() {});
  }

}
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

import java.util.Map;

import lombok.SneakyThrows;
import lombok.val;
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
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ComputeBolt extends BaseRichBolt implements UpdateListener {

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
    val configuration = new Configuration();

    this.collector = collector;
    this.esperSink = EPServiceProviderManager.getProvider(this.toString(), configuration);
    this.esperSink.initialize();
    this.runtime = esperSink.getEPRuntime();
    this.admin = esperSink.getEPAdministrator();

    // @formatter:off
    EPStatement statement = admin.createEPL(
        "SELECT " +
        "  CAST(ask, float) / CAST(prior(1, ask), float) AS askPercentChange " +
        "FROM " +
        "  " + Rate.class.getName() + ""
        );
    // @formatter:on

    statement.addListener(this);
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
      for (EventBean newEvent : newEvents) {
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

}
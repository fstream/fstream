/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.esper;

import static io.fstream.core.util.Maps.getProperties;
import io.fstream.core.config.KafkaProperties;
import io.fstream.core.model.definition.Alert;
import io.fstream.core.model.definition.Definition;
import io.fstream.core.model.event.AlertEvent;
import io.fstream.core.model.event.Event;
import io.fstream.core.model.event.EventType;
import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Quote;
import io.fstream.core.model.event.Trade;
import io.fstream.core.model.state.State;
import io.fstream.core.model.topic.Topic;
import io.fstream.core.util.Codec;

import java.util.List;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.StatementAwareUpdateListener;
import com.espertech.esper.client.metric.MetricEvent;
import com.espertech.esper.client.time.CurrentTimeEvent;

@Slf4j
public class EsperJob implements StatementAwareUpdateListener {

  /**
   * Constants.
   */
  private static final String KAFKA_TOPIC_KEY = "1";

  private final State state;
  @Autowired
  private final KafkaProperties kafka;

  /**
   * Esper.
   */
  private final EPServiceProvider provider;
  private final EPRuntime runtime;
  private final EPAdministrator admin;

  /**
   * Kafka.
   */
  private final Producer<String, String> pruducer;

  private final boolean externalClock = false;

  public EsperJob(String jobId, State state, KafkaProperties kafka) {
    this.state = state;
    this.kafka = kafka;
    this.pruducer = createProducer();

    val configuration = createConfiguration();
    if (externalClock) {
      // Set external clock for esper
      configuration.getEngineDefaults().getThreading().setInternalTimerEnabled(false);
    }

    this.provider = EPServiceProviderManager.getProvider(this.toString(), configuration);
    this.provider.initialize();
    this.runtime = provider.getEPRuntime();
    this.admin = provider.getEPAdministrator();

    log.info("[{}] Creating common statements...", jobId);
    for (val statement : getStatements()) {
      log.info("Registering common statement: '{};", statement.replace('\n', ' '));
      val epl = admin.createEPL(statement);

      epl.addListener(this);
    }
    log.info("Finished creating common statements.");

    // Delegate to child
    log.info("Creating '{}' statements...", this.getClass().getSimpleName());
    createStatements(admin);
    log.info("Finished creating '{}' statements.", this.getClass().getSimpleName());
  }

  private Event createEvent(int id, Object data) {
    return new AlertEvent(new DateTime(), id, data);
  }

  private Configuration createConfiguration() {
    val configuration = new Configuration();
    configuration.configure();
    configuration.addEventType("Trade", Trade.class.getName());
    configuration.addEventType("Order", Order.class.getName());
    configuration.addEventType("Quote", Quote.class.getName());
    configuration.addEventType("Alert", AlertEvent.class.getName());
    configuration.addEventType("Metric", MetricEvent.class.getName());

    return configuration;
  }

  @SneakyThrows
  public void execute(Event event) {
    if (externalClock) {
      // Send external timer event - timestamp of incoming event.
      // Quote are truly external events. Rest are internally generated.
      if (event.getType().equals(EventType.QUOTE)) {
        runtime.sendEvent(new CurrentTimeEvent(event.getDateTime().getMillis()));
      }
    }

    runtime.sendEvent(event);
  }

  @Override
  public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement,
      EPServiceProvider epServiceProvider) {
    if (newEvents != null) {
      val definition = (Definition) statement.getUserObject();
      for (val newEvent : newEvents) {
        val data = newEvent.getUnderlying();
        val event = createEvent(definition.getId(), data);

        val value = Codec.encodeText(event);
        val topicName = resolveTopic(definition).getId();
        val message = new KeyedMessage<String, String>(topicName, KAFKA_TOPIC_KEY, value);
        pruducer.send(message);
      }
    }
  }

  private Topic resolveTopic(Definition definition) {
    return definition instanceof Alert ? Topic.ALERTS : Topic.METRICS;
  }

  protected void createStatements(EPAdministrator admin) {
    val alerts = state.getAlerts();
    for (val alert : alerts) {
      log.info("Registering alert: {}", alert.getName());
      val statement = admin.createEPL(alert.getStatement(), alert);

      statement.addListener(this);
    }

    val metrics = state.getMetrics();
    for (val metric : metrics) {
      log.info("Registering metric: {}", metric.getName());
      val statement = admin.createEPL(metric.getStatement(), metric);

      statement.addListener(this);
    }
  }

  public void cleanup() {
    if (provider != null) {
      provider.destroy();
    }
  }

  @SneakyThrows
  private List<String> getStatements() {
    return state.getStatements();
  }

  @SneakyThrows
  private Producer<String, String> createProducer() {
    val producerConfig = new ProducerConfig(getProperties(kafka.getProducerProperties()));

    return new Producer<>(producerConfig);
  }

}
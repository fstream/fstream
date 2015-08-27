/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.esper;

import io.fstream.core.model.definition.Alert;
import io.fstream.core.model.definition.Definition;
import io.fstream.core.model.event.AlertEvent;
import io.fstream.core.model.event.Event;
import io.fstream.core.model.event.EventType;
import io.fstream.core.model.event.MetricEvent;
import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Quote;
import io.fstream.core.model.event.Trade;
import io.fstream.core.model.state.State;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.StatementAwareUpdateListener;
import com.espertech.esper.client.time.CurrentTimeEvent;

@Slf4j
public class EsperJob implements StatementAwareUpdateListener {

  /**
   * Configuration.
   */
  @Getter
  private final String jobId;
  private final State state;

  /**
   * Dependencies
   */
  private final EsperProducer producer;
  private final EPServiceProvider provider;
  private final EPRuntime runtime;
  private final EPAdministrator admin;

  /**
   * Configuration.
   */
  private final boolean externalClock = false;

  public EsperJob(String jobId, State state, EsperProducer producer) {
    this.jobId = jobId;
    this.state = state;
    this.producer = producer;

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
    for (val statement : state.getStatements()) {
      log.info("Registering common statement: '{};", statement.replace('\n', ' '));
      try {
        val epl = admin.createEPL(statement);

        epl.addListener(this);
      } catch (Exception e) {
        log.error("Error registering esper statement: {}", e);
      }
    }
    log.info("Finished creating common statements.");

    // Delegate to child
    log.info("Creating '{}' statements...", this.getClass().getSimpleName());
    createStatements(admin);
    log.info("Finished creating '{}' statements.", this.getClass().getSimpleName());
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
        val event = createEvent(definition, data);

        producer.send(event);
      }
    }
  }

  public void stop() {
    if (provider != null) {
      provider.destroy();
    }
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

  private void createStatements(EPAdministrator admin) {
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

  private Event createEvent(Definition definition, Object data) {
    val id = definition.getId();
    return definition instanceof Alert ?
        new AlertEvent(new DateTime(), id, data) :
        new MetricEvent(new DateTime(), id, data);
  }

}
/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.stream.compute;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Resources.readLines;
import static org.assertj.core.util.Lists.newArrayList;
import io.fstream.core.model.event.TickEvent;

import java.io.File;
import java.util.List;

import lombok.SneakyThrows;
import lombok.val;

import org.junit.After;
import org.junit.Before;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;

public abstract class AbstractEsperStatementTest implements UpdateListener {

  private static final ObjectMapper MAPPER = new ObjectMapper()
      .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
      .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

  /**
   * Esper state.
   */
  private EPServiceProvider esperSink;
  private EPRuntime runtime;
  private EPAdministrator admin;

  private List<Object> results;

  @Before
  public void setUp() {
    val configuration = new Configuration();
    configuration.addEventType("Rate", TickEvent.class.getName());

    this.esperSink = EPServiceProviderManager.getProvider(this.toString(), configuration);
    this.esperSink.initialize();
    this.runtime = esperSink.getEPRuntime();
    this.admin = esperSink.getEPAdministrator();
  }

  @SneakyThrows
  protected List<Object> execute(File eplFile, TickEvent... events) {
    return execute(eplFile, events);
  }

  @SneakyThrows
  protected List<Object> execute(File eplFile, Iterable<TickEvent> events) {
    return execute(Resources.toString(eplFile.toURI().toURL(), UTF_8), events);
  }

  @SneakyThrows
  protected List<Object> execute(File eplFile, File tickEventFile) {
    return execute(Resources.toString(eplFile.toURI().toURL(), UTF_8), getTicketEvents(tickEventFile));
  }

  protected List<Object> execute(String statement, TickEvent... events) {
    return execute(statement, events);
  }

  protected List<Object> execute(String statement, Iterable<TickEvent> events) {
    val epl = admin.createEPL(statement);

    epl.addListener(this);
    for (val event : events) {
      runtime.sendEvent(event);
    }

    return results;
  }

  @Override
  public void update(EventBean[] newEvents, EventBean[] oldEvents) {
    this.results = newArrayList();
    if (newEvents != null) {
      for (val newEvent : newEvents) {
        results.add(newEvent.getUnderlying());
      }
    }
  }

  @After
  public void tearDown() {
    if (esperSink != null) {
      esperSink.destroy();
    }
  }

  @SneakyThrows
  private static Iterable<TickEvent> getTicketEvents(File tickEventFile) {
    val lines = readLines(tickEventFile.toURI().toURL(), UTF_8);

    val builder = ImmutableList.<TickEvent> builder();
    for (val line : lines) {
      val tickEvent = MAPPER.readValue(line, TickEvent.class);
      builder.add(tickEvent);
    }

    return builder.build();
  }

  protected static List<TickEvent> tickEvents(TickEvent... tickEvents) {
    return ImmutableList.copyOf(tickEvents);
  }

  protected static File eplFile(String fileName) {
    return new File("src/test/resources/epl", fileName);
  }

  protected static File tickEventFile(String fileName) {
    return new File("src/test/resources/tick-events", fileName);
  }

}

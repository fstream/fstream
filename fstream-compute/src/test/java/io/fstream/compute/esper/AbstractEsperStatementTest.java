/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.esper;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Resources.readLines;
import static joptsimple.internal.Strings.repeat;
import io.fstream.core.model.event.Quote;

import java.io.File;
import java.util.List;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Before;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esper.client.time.TimerControlEvent;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;

@Slf4j
public abstract class AbstractEsperStatementTest {

  /**
   * Constants.
   */
  private static final DateTimeFormatter SECONDS_PATTERN = DateTimeFormat.forPattern("HH:mm:ss");
  private static final ObjectMapper MAPPER = new ObjectMapper()
      .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
      .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

  /**
   * Esper state.
   */
  private EPServiceProvider provider;
  private EPRuntime runtime;
  private EPAdministrator admin;

  @Before
  public void setUp() {
    val configuration = new Configuration();
    configuration.addEventType("Quote", Quote.class.getName());

    // Setup engine
    this.provider = EPServiceProviderManager.getProvider(this.getClass().getName(), configuration);

    // Shorthands
    this.runtime = provider.getEPRuntime();
    this.admin = provider.getEPAdministrator();

    // Use "external clocking" for the test
    val clockType = TimerControlEvent.ClockType.CLOCK_EXTERNAL;
    log.info("Setting clock type to '{}'", clockType);
    runtime.sendEvent(new TimerControlEvent(clockType));
  }

  @SneakyThrows
  protected List<?> execute(File eplFile, Iterable<?> events) {
    return execute(Resources.toString(eplFile.toURI().toURL(), UTF_8), events);
  }

  @SneakyThrows
  protected List<?> execute(File eplFile, File quoteEventFile) {
    return execute(Resources.toString(eplFile.toURI().toURL(), UTF_8), readQuoteEvents(quoteEventFile));
  }

  protected List<?> execute(String statement, Quote... events) {
    return execute(statement, events);
  }

  protected List<?> execute(String statement, Iterable<?> events) {
    // Statement results
    val results = Lists.<Object> newArrayList();

    // Setup
    val epl = admin.createEPL(statement);
    epl.addListener(new UpdateListener() {

      @Override
      public void update(EventBean[] newEvents, EventBean[] oldEvents) {
        if (newEvents == null) {
          return;
        }

        // Buffer results
        for (val newEvent : newEvents) {
          val result = newEvent.getUnderlying();

          // Log results
          log.info(repeat('-', 80));
          log.info("Result: {}", result);
          log.info(repeat('-', 80));

          results.add(result);
        }

      }
    });

    // Apply stimulus
    log.info(repeat('-', 80));
    log.info("Executing: {}", statement);
    log.info(repeat('-', 80));

    for (val event : events) {
      if (event instanceof Quote) {
        // Advance time
        val quoteEvent = (Quote) event;
        val timeEvent = timeEvent(quoteEvent.getDateTime().getMillis());
        log.info("Sending: {}", timeEvent);
        runtime.sendEvent(timeEvent);
      }

      // Send event
      log.info("Sending: {}", event);
      runtime.sendEvent(event);
    }

    // Allow the client to analyze
    return results;
  }

  @After
  public void tearDown() {
    if (provider != null) {
      provider.destroy();
    }
  }

  protected static String epl(String epl) {
    return epl;
  }

  protected static File eplFile(String fileName) {
    return new File("src/test/resources/epl", fileName);
  }

  protected static List<?> givenEvents(Object... events) {
    return ImmutableList.copyOf(events);
  }

  protected static File givenEvents(File eventFile) {
    return eventFile;
  }

  protected static long second(String value) {
    return DateTime.parse(value, SECONDS_PATTERN).getMillis();
  }

  protected static CurrentTimeEvent timeEvent(long time) {
    return new CurrentTimeEvent(time);
  }

  protected static File quoteEventFile(String fileName) {
    return new File("src/test/resources/quote-events", fileName);
  }

  protected static Quote quoteEvent(long time, String symbol, double ask, double bid) {
    return new Quote(new DateTime(time), symbol, (float) ask, (float) bid);
  }

  @SneakyThrows
  private static Iterable<Quote> readQuoteEvents(File quoteEventFile) {
    val lines = readLines(quoteEventFile.toURI().toURL(), UTF_8);

    val builder = ImmutableList.<Quote> builder();
    for (val line : lines) {
      val quoteEvent = MAPPER.readValue(line, Quote.class);
      builder.add(quoteEvent);
    }

    return builder.build();
  }

}

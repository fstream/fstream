/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.core;

import static com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE;
import static java.util.Comparator.comparing;
import io.fstream.core.model.event.AbstractEvent;
import io.fstream.core.model.event.Event;
import io.fstream.core.model.event.EventType;
import io.fstream.simulate.routes.PublishRoutes;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;

import javax.annotation.PostConstruct;

import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.camel.ProducerTemplate;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AbstractExecutionThreadService;

/**
 * Replays event streams from a specified directory of JSON files at the specified speed.'
 */
@Slf4j
@Setter
@Component
@Profile("replay")
public class Replayer extends AbstractExecutionThreadService {

  /**
   * Constants.
   */
  private static final ObjectMapper MAPPER = new ObjectMapper().configure(AUTO_CLOSE_SOURCE, false);

  /**
   * Configuration.
   */
  @Value("${simulate.file.dir}")
  private File inputDir;
  @Value("${simulate.file.delay}")
  private long delay;
  @Value("${simulate.file.speed}")
  private float speed;
  @Value("${simulate.file.loop}")
  private boolean loop;

  /**
   * Dependencies.
   */
  @Autowired
  private ProducerTemplate template;

  /**
   * State.
   */
  private long currentTime;
  private long cycleDuration;

  @PostConstruct
  public void init() throws Exception {
    log.info("Replaying event streams...");
    startAsync();
  }

  @Override
  protected void run() throws Exception {
    // Wait for destination camel route (heuristic)
    Thread.sleep(delay);

    log.info("Calculating interval...");
    val interval = getInterval();
    log.info("Calculated interval: {}", interval);

    // The duration of the entire cycle
    this.cycleDuration = interval.toDurationMillis();
    log.info("Calculated cycle duration: {} ms", cycleDuration);

    // Open streams
    val eventStreams = openEventStreams();
    if (eventStreams.isEmpty()) {
      log.warn("*** No streams available. Exiting...");
      stopAsync();
      return;
    }

    // Initialize time ordered queue of blended events
    val eventQueue = createEventQueue();
    for (val stream : eventStreams.values()) {
      eventQueue.add(stream.next());
    }

    // Initialize time based on first event
    this.currentTime = interval.getStart().getMillis();

    while (!eventQueue.isEmpty()) {
      // Next event in time order
      val event = eventQueue.remove();

      //
      // Schedule
      //

      val eventTime = event.getDateTime().getMillis();
      if (eventTime > currentTime) {
        // Simulate time delay to make current
        val delay = calculateDelay(currentTime, eventTime);
        Thread.sleep(delay);

        // Advance time
        currentTime = eventTime;
      }

      // Make it current
      ((AbstractEvent) event).setDateTime(DateTime.now());

      //
      // Publish
      //

      publish(event);

      // Replenish event of this type
      val eventStream = eventStreams.get(event.getType());
      if (eventStream.hasNext()) {
        eventQueue.add(eventStream.next());
      }
    }
  }

  private long calculateDelay(long currentTime, long eventTime) {
    // Modulate delay based on speed.
    return (long) ((eventTime - currentTime) / speed);
  }

  private void publish(Event event) {
    template.sendBody(PublishRoutes.PUBLISH_ENDPOINT, event);
  }

  @SneakyThrows
  private Map<EventType, Iterator<Event>> openEventStreams() {
    val eventStreams = Maps.<EventType, Iterator<Event>> newHashMap();
    for (val eventType : EventType.values()) {
      val eventFile = getEventFile(eventType);
      if (eventFile.exists()) {
        log.info("*** Reading {} from {}", eventType, eventFile);

        val eventStream = openEventStream(eventFile);
        eventStreams.put(eventType, eventStream);
      }
    }

    return eventStreams;
  }

  private File getEventFile(EventType eventType) {
    return new File(inputDir, getEventFileName(eventType));
  }

  private static String getEventFileName(EventType eventType) {
    val name = eventType.name().toLowerCase() + "s";
    return "fstream-simulate-" + name + ".json";
  }

  private static PriorityQueue<Event> createEventQueue() {
    // Prioritize by time
    return new PriorityQueue<>(comparing(e -> e.getDateTime()));
  }

  private static void setDateTime(Event event, DateTime dateTime) {
    ((AbstractEvent) event).setDateTime(dateTime);
  }

  private Interval getInterval() {
    DateTime minTime = null;
    DateTime maxTime = null;

    for (val eventType : EventType.values()) {
      val eventFile = getEventFile(eventType);
      if (eventFile.exists()) {
        val eventFileInterval = getEventFileInterval(eventFile);
        if (minTime == null || eventFileInterval.getStart().isBefore(minTime)) {
          minTime = eventFileInterval.getStart();
        }
        if (maxTime == null || eventFileInterval.getEnd().isAfter(maxTime)) {
          maxTime = eventFileInterval.getEnd();
        }
      }
    }

    return new Interval(minTime, maxTime);
  }

  @SneakyThrows
  private Interval getEventFileInterval(File file) {
    MappingIterator<Event> iterator = MAPPER.reader(Event.class).readValues(file);

    DateTime minTime = null;
    DateTime maxTime = null;
    while (iterator.hasNext()) {
      val eventTime = iterator.next().getDateTime();

      if (minTime == null || eventTime.isBefore(minTime)) {
        minTime = eventTime;
      }
      if (maxTime == null || eventTime.isAfter(maxTime)) {
        maxTime = eventTime;
      }
    }

    return new Interval(minTime, maxTime);
  }

  private Iterator<Event> openEventStream(File file) {
    return new Iterator<Event>() {

      MappingIterator<Event> delegate = open();

      int eventCount = 0;
      int cycleCount = 0;

      @Override
      @SneakyThrows
      public boolean hasNext() {
        if (!loop) {
          return delegate.hasNext();
        }

        if (!delegate.hasNext()) {
          delegate.close();
          delegate = open();
          cycleCount++;
          log.info("[cycle: {}, event: {}] Cycling file {}", cycleCount, eventCount, file);
        }

        return true;
      }

      @Override
      public Event next() {
        val event = delegate.next();
        eventCount++;

        // Advance time
        val delta = cycleCount * cycleDuration;
        val newDateTime = event.getDateTime().plus(delta);
        setDateTime(event, newDateTime);

        return event;
      }

      @SneakyThrows
      private MappingIterator<Event> open() {
        return MAPPER.reader(Event.class).readValues(file);
      }

    };
  }

}
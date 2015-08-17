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
import io.fstream.core.model.event.AbstractEvent;
import io.fstream.core.model.event.Event;
import io.fstream.core.model.event.EventType;
import io.fstream.simulate.routes.PublishRoutes;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;

import javax.annotation.PostConstruct;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.camel.ProducerTemplate;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AbstractExecutionThreadService;

/**
 * Replays event streams from a specified directory of JSON files at the specified speed.'
 */
@Slf4j
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
  @Value("${simulate.file.speed}")
  private float speed;

  /**
   * Dependencies.
   */
  @Autowired
  private ProducerTemplate template;

  @PostConstruct
  public void init() throws Exception {
    log.info("Replaying event streams...");
    startAsync();
  }

  @Override
  protected void run() throws Exception {
    // Wait for destination camel route (heuristic)
    Thread.sleep(5000);

    // Open streams
    val eventStreams = openEventStreams();

    // Initialize time ordered queue of blended events
    val eventQueue = createEventQueue();
    for (val stream : eventStreams.values()) {
      eventQueue.add(stream.next());
    }

    // Initialize time based on first event
    long currentTime = eventQueue.peek().getDateTime().getMillis();

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
  private Map<EventType, MappingIterator<Event>> openEventStreams() {
    val eventStreams = Maps.<EventType, MappingIterator<Event>> newHashMap();
    for (val eventType : EventType.values()) {
      val eventFile = new File(inputDir, getEventFileName(eventType));
      if (eventFile.exists()) {
        log.info("*** Reading {} from {}", eventType, eventFile);
        eventStreams.put(eventType, openEventStream(eventFile));
      }
    }

    return eventStreams;
  }

  private static String getEventFileName(EventType eventType) {
    val name = eventType.name().toLowerCase() + "s";
    return "fstream-simulate-" + name + ".json";
  }

  private static PriorityQueue<Event> createEventQueue() {
    // Prioritize by time
    return new PriorityQueue<Event>(Comparator.<Event, DateTime> comparing(e -> e.getDateTime()));
  }

  private static MappingIterator<Event> openEventStream(File file) throws JsonProcessingException, IOException {
    return MAPPER.reader(Event.class).readValues(file);
  }

}
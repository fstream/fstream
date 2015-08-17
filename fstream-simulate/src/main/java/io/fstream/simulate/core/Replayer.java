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
import static io.fstream.core.model.event.EventType.ORDER;
import static io.fstream.core.model.event.EventType.QUOTE;
import static io.fstream.core.model.event.EventType.TRADE;
import io.fstream.core.model.event.AbstractEvent;
import io.fstream.core.model.event.Event;
import io.fstream.core.model.event.EventType;
import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Quote;
import io.fstream.core.model.event.Snapshot;
import io.fstream.core.model.event.Trade;
import io.fstream.simulate.routes.PublishRoutes;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;

import javax.annotation.PostConstruct;

import lombok.Cleanup;
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
import com.google.common.util.concurrent.AbstractExecutionThreadService;

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

  @Autowired
  private ProducerTemplate template;

  @PostConstruct
  public void init() throws Exception {
    log.info("Replaying...");
    startAsync();
  }

  @Override
  protected void run() throws Exception {
    // Wait for route
    Thread.sleep(5000);

    @Cleanup
    val trades = read("fstream-simulate-trades.json", Trade.class);
    @Cleanup
    val orders = read("fstream-simulate-orders.json", Order.class);
    @Cleanup
    val quotes = read("fstream-simulate-quotes.json", Quote.class);
    @Cleanup
    val snapshots = read("fstream-simulate-snapshots.json", Snapshot.class);

    val events = createEventQueue();
    events.add(trades.next());
    events.add(orders.next());
    events.add(quotes.next());
    events.add(snapshots.next());

    long currentTime = events.peek().getDateTime().getMillis();

    while (!events.isEmpty()) {
      // Next event
      val event = events.remove();

      val eventTime = event.getDateTime().getMillis();
      if (eventTime > currentTime) {
        // Modulate delay based on speed.
        val delay = (eventTime - currentTime) / speed;

        // Simulate time delay
        Thread.sleep((long) delay);
        currentTime = eventTime;
      }

      event.setDateTime(DateTime.now());
      send(event);

      if (event.getType() == TRADE && trades.hasNext()) {
        events.add(trades.next());
      }
      if (event.getType() == ORDER && orders.hasNext()) {
        events.add(orders.next());
      }
      if (event.getType() == QUOTE && quotes.hasNext()) {
        events.add(quotes.next());
      }
      if (event.getType() == EventType.SNAPSHOT && snapshots.hasNext()) {
        events.add(snapshots.next());
      }
    }
  }

  private void send(Event event) {
    template.sendBody(PublishRoutes.PUBLISH_ENDPOINT, event);
  }

  private static PriorityQueue<AbstractEvent> createEventQueue() {
    return new PriorityQueue<AbstractEvent>(Comparator.<AbstractEvent, DateTime> comparing(e -> e.getDateTime()));
  }

  private <T> MappingIterator<T> read(String fileName, Class<T> eventType) throws JsonProcessingException, IOException {
    val reader = MAPPER.reader(eventType);

    return reader.readValues(new File(inputDir, fileName));
  }

}
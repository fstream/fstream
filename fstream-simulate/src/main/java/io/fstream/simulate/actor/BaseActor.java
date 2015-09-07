/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.actor;

import static com.google.common.collect.Lists.newArrayList;
import io.fstream.core.model.event.Event;
import io.fstream.core.model.event.EventType;
import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.util.EventStats;

import java.util.List;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;

import org.joda.time.DateTime;

import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import akka.util.Timeout;

/**
 * Base class for all simulation actors.
 * <p>
 * Contains a number of common fields and convenience methods.
 */
@Setter
@RequiredArgsConstructor
public abstract class BaseActor extends UntypedActor {

  /**
   * Configuration.
   */
  @NonNull
  protected final SimulateProperties properties;

  /**
   * Statistics.
   */
  protected final EventStats stats = new EventStats();

  /**
   * State.
   */
  protected List<String> activeInstruments = newArrayList();

  private ActorSelection exchange() {
    return context().actorSelection("/user/exchange");
  }

  private ActorSelection publisher() {
    return context().actorSelection("/user/publisher");
  }

  protected static DateTime getSimulationTime() {
    return DateTime.now();
  }

  protected void exchangeMessage(@NonNull Object message) {
    exchange().tell(message, self());
  }

  protected Future<Object> exchangeAsk(@NonNull Object message, Timeout timeout) {
    return Patterns.ask(exchange(), message, timeout);
  }

  protected void publishEvent(@NonNull Event event) {
    // Update stats
    if (event.getType() == EventType.TRADE) {
      stats.incrementTradeCount();
    } else if (event.getType() == EventType.ORDER) {
      stats.incrementOrderCount();
    } else if (event.getType() == EventType.QUOTE) {
      stats.incrementQuoteCount();
    } else if (event.getType() == EventType.SNAPSHOT) {
      stats.incrementSnapshotCount();
    }

    publisher().tell(event, self());
  }

  @NonNull
  protected <T> void scheduleSelfOnce(T message, FiniteDuration duration) {
    val scheduler = getContext().system().scheduler();
    val dispatcher = getContext().dispatcher();
    scheduler.scheduleOnce(duration, getSelf(), message, dispatcher, null);
  }

  protected boolean isActive(String symbol) {
    return activeInstruments.contains(symbol);
  }

}

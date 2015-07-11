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
import io.fstream.simulate.config.SimulateProperties;

import java.util.List;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;

import org.joda.time.DateTime;

import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorSelection;
import akka.actor.UntypedActor;

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
   * State.
   */
  protected List<String> activeInstruments = newArrayList();

  protected ActorSelection exchange() {
    return context().actorSelection("/user/exchange");
  }

  protected ActorSelection publisher() {
    return context().actorSelection("/user/publisher");
  }

  protected static DateTime getSimulationTime() {
    return DateTime.now();
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

/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.actor;

import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.message.ActiveInstruments;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;

import org.joda.time.DateTime;

import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorSelection;
import akka.actor.UntypedActor;

@Setter
@RequiredArgsConstructor
public abstract class BaseActor extends UntypedActor {

  /**
   * Dependencies.
   */
  @NonNull
  protected final SimulateProperties properties;

  /**
   * State.
   */
  protected ActiveInstruments activeInstruments = new ActiveInstruments();

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
    scheduler.scheduleOnce(duration, getSelf(), message, getContext().dispatcher(), null);
  }

}

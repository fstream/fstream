/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.analyze.util;

import static lombok.AccessLevel.PRIVATE;
import io.fstream.core.model.event.Event;
import io.fstream.core.model.event.EventType;
import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Trade;
import io.fstream.core.util.Codec;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import org.apache.spark.api.java.function.Function;

import scala.Tuple2;

@NoArgsConstructor(access = PRIVATE)
public final class EventFunctions {

  public static Function<Tuple2<String, String>, Order> parseOrder() {
    return tuple -> Codec.decodeText(tuple._2, Order.class);
  }

  public static Function<Tuple2<String, String>, Trade> parseTrade() {
    return tuple -> Codec.decodeText(tuple._2, Trade.class);
  }

  public static Function<Tuple2<String, String>, Event> parseEvent() {
    return tuple -> Codec.decodeText(tuple._2, Event.class);
  }

  @NonNull
  public static Function<Event, Boolean> filterEventType(EventType eventType) {
    return event -> event.getType() == eventType;
  }

  @NonNull
  @SuppressWarnings("unchecked")
  public static <T extends Event> Function<Event, T> castEvent(Class<T> t) {
    return event -> (T) event;
  }

}

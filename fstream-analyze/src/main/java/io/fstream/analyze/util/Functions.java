/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.analyze.util;

import io.fstream.core.model.event.Event;
import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Trade;
import io.fstream.core.util.Codec;

import java.util.List;

import lombok.val;

import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;

import scala.Tuple2;

import com.google.common.base.Optional;

public class Functions {

  public static Function2<List<Long>, Optional<Long>, Optional<Long>> computeLongRunningSum() {
    return (values, state) -> {
      long sum = state.or(0L);
      for (val i : values) {
        sum += i;
      }

      return Optional.of(sum);
    };
  }

  public static Function2<List<Integer>, Optional<Integer>, Optional<Integer>> computeIntegerRunningSum() {
    return (values, state) -> {
      int sum = state.or(0);
      for (val i : values) {
        sum += i;
      }

      return Optional.of(sum);
    };
  }

  public static Function2<List<Float>, Optional<Float>, Optional<Float>> computeFloatRunningSum() {
    return (values, state) -> {
      float sum = state.or(0f);
      for (val i : values) {
        sum += i;
      }

      return Optional.of(sum);
    };
  }

  public static final Function2<Long, Long, Long> sumLongReducer() {
    return (a, b) -> a + b;
  }

  public static final Function2<Integer, Integer, Integer> sumIntegerReducer() {
    return (a, b) -> a + b;
  }

  public static final Function2<Float, Float, Float> sumFloatReducer() {
    return (a, b) -> a + b;
  }

  public static Function<Tuple2<String, String>, Order> parseOrder() {
    return tuple -> Codec.decodeText(tuple._2, Order.class);
  }

  public static Function<Tuple2<String, String>, Trade> parseTrade() {
    return tuple -> Codec.decodeText(tuple._2, Trade.class);
  }

  public static Function<Tuple2<String, String>, Event> parseEvent() {
    return tuple -> Codec.decodeText(tuple._2, Event.class);
  }

}

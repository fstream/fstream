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

import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;

import scala.Tuple2;

import com.google.common.base.Optional;

public class Functions {

  public static Function2<List<Long>, Optional<Long>, Optional<Long>> computeRunningSum() {
    return new Function2<List<Long>, Optional<Long>, Optional<Long>>() {

      @Override
      public Optional<Long> call(List<Long> nums, Optional<Long> current) throws Exception {
        long sum = current.or(0L);
        for (long i : nums) {
          sum += i;
        }

        return Optional.of(sum);
      }

    };
  }

  public static final Function2<Long, Long, Long> sumReducer() {
    return (a, b) -> a + b;
  }

  public static Function<Tuple2<String, String>, Order> parseOrder() {
    return tuple -> Codec.decodeText(tuple._2, Order.class);
  }

  public static Function<Tuple2<String, String>, Trade> parseTrade() {
    return tuple -> Codec.decodeText(tuple._2, Trade.class);
  }

  public static Function<Tuple2<String, String>, Event> parseEvents() {
    return tuple -> Codec.decodeText(tuple._2, Event.class);
  }

  public static PairFunction<Order, String, Long> mapUserIdAmount() {
    return order -> new Tuple2<String, Long>(order.getUserId(), (long) order.getAmount());
  }

}

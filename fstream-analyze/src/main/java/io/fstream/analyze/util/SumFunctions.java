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

import java.util.List;

import lombok.NoArgsConstructor;
import lombok.val;

import org.apache.spark.api.java.function.Function2;

import com.google.common.base.Optional;

@NoArgsConstructor(access = PRIVATE)
public final class SumFunctions {

  public static Function2<Integer, Integer, Integer> sumIntegers() {
    return (a, b) -> a + b;
  }

  public static Function2<Long, Long, Long> sumLongs() {
    return (a, b) -> a + b;
  }

  public static Function2<Float, Float, Float> sumFloats() {
    return (a, b) -> a + b;
  }

  public static Function2<List<Integer>, Optional<Integer>, Optional<Integer>> runningSumIntegers() {
    return (values, state) -> {
      int sum = state.or(0);
      for (val i : values) {
        sum += i;
      }
  
      return Optional.of(sum);
    };
  }

  public static Function2<List<Long>, Optional<Long>, Optional<Long>> runningSumLongs() {
    return (values, state) -> {
      long sum = state.or(0L);
      for (val i : values) {
        sum += i;
      }

      return Optional.of(sum);
    };
  }

  public static Function2<List<Float>, Optional<Float>, Optional<Float>> runningSumFloats() {
    return (values, state) -> {
      float sum = state.or(0F);
      for (val i : values) {
        sum += i;
      }

      return Optional.of(sum);
    };
  }

}

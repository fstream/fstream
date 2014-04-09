/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.core.util;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PRIVATE;
import static org.joda.time.Duration.standardSeconds;

import java.util.concurrent.TimeUnit;

import lombok.NoArgsConstructor;
import lombok.val;

import org.joda.time.Duration;
import org.joda.time.Period;

import com.google.common.base.Stopwatch;

@NoArgsConstructor(access = PRIVATE)
public final class FormatUtils {

  public static String _(String format, Object... args) {
    return String.format(format, args);
  }

  public static String formatBytes(long bytes) {
    return formatBytes(bytes, true);
  }

  public static String formatBytes(long bytes, boolean si) {
    int unit = si ? 1000 : 1024;
    if (bytes < unit) return bytes + " B";

    int exp = (int) (Math.log(bytes) / Math.log(unit));
    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");

    return format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
  }

  public static String formatCount(int count) {
    return format("%,d", count);
  }

  public static String formatCount(long count) {
    return format("%,d", count);
  }

  public static String formatRate(float rate) {
    return format("%,.2f", rate);
  }

  public static String formatRate(int count, Stopwatch watch) {
    return formatRate(rate(watch, count));
  }

  public static String formatPercent(float percent) {
    return format("%.2f", percent);
  }

  public static String formatPercent(double percent) {
    return format("%.2f", percent);
  }

  public static String formatDuration(Stopwatch watch) {
    return formatDuration(watch.elapsed(SECONDS));
  }

  public static String formatDuration(long seconds) {
    Duration duration = standardSeconds(seconds);

    return formatPeriod(duration.toPeriod());
  }

  public static String formatPeriod(Period period) {
    period = period.normalizedStandard();

    return format("%02d:%02d:%02d:%02d (dd:hh:mm:ss)",
        period.getDays(), period.getHours(), period.getMinutes(), period.getSeconds());
  }

  public static String formatMemory() {
    val runtime = Runtime.getRuntime();

    return new StringBuilder()
        .append("max memory: ")
        .append(formatBytes(runtime.maxMemory()))
        .append(", total memory: ")
        .append(formatBytes(runtime.totalMemory()))
        .append(", free memory: ")
        .append(formatBytes(runtime.freeMemory()))
        .toString();
  }

  private static float rate(Stopwatch watch, int count) {
    float seconds = watch.elapsed(TimeUnit.MILLISECONDS) / 1000.0f;
    if (seconds == 0.0f) {
      return 0.0f;
    }

    return count / seconds;
  }

}
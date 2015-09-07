/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist.parquet;

import io.fstream.core.model.event.Event;
import io.fstream.core.model.event.EventType;

import java.io.IOException;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.hadoop.fs.Path;

@Slf4j
@Getter
public class RollingEventParquetWriter implements EventParquetWriter {

  /**
   * Constants.
   */
  private static final long DEFAULT_THRESHOLD_EVENT_COUNT = 1_000_000;

  /**
   * Configuration.
   */
  private EventType type;
  private Path outputFile;
  private long thresholdEventCount;

  /**
   * Statistics.
   */
  private long rollCount;
  private long eventCount;

  /**
   * State.
   */
  private EventParquetWriter delegate;

  public RollingEventParquetWriter(EventType type, Path outputFile) {
    this(type, outputFile, DEFAULT_THRESHOLD_EVENT_COUNT);
  }

  @SneakyThrows
  public RollingEventParquetWriter(@NonNull EventType type, @NonNull Path outputFile, long thresholdEventCount) {
    this.type = type;
    this.outputFile = outputFile;
    this.thresholdEventCount = thresholdEventCount;
    roll();
  }

  @Override
  @Synchronized
  public void write(Event event) {
    eventCount++;
    if (eventCount % thresholdEventCount == 0) {
      roll();
    }

    delegate.write(event);
  }

  @Override
  @Synchronized
  public void close() throws IOException {
    delegate.close();
  }

  @SneakyThrows
  private void roll() {
    rollCount++;
    if (delegate != null) {
      close();
    }

    delegate = createWriter();
  }

  private EventParquetWriter createWriter() {
    val path = new Path(outputFile.toString() + "." + String.format("%04d", rollCount));

    log.info("Creating '{}' writer for file '{}'", type, path);
    return new BasicEventParquetWriter(type, path);
  }

}

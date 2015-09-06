/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist.parquet;

import static com.google.common.base.Strings.repeat;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import io.fstream.core.model.event.Quote;
import io.fstream.persist.parquet.JsonParquetConverter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.Function;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

@Ignore
@Slf4j
public class JsonParquetConverterTest {

  /**
   * Test data.
   */
  String inputFile = "build/data.json";
  String outputFile = "build/data.parquet";

  @Before
  public void setUp() {
    deleteQuietly(new File(inputFile));
    deleteQuietly(new File(outputFile));
  }

  @Test
  public void testJsonParquet() throws Exception {
    log.info(repeat("-", 100));
    log.info("Simulate some data to json file...");
    log.info(repeat("-", 100));

    val lineCount = 1_000_000_000;
    createJsonFile(inputFile, lineCount, this::createValue);

    log.info(repeat("-", 100));
    log.info("Converting json to parquet...");
    log.info(repeat("-", 100));
    convertJsonFile(inputFile, outputFile);
  }

  private static void createJsonFile(String inputFile, int lineCount, Function<Float, Object> generator)
      throws IOException {
    try (val writer = new PrintWriter(inputFile)) {
      val mapper = createMapper();
      for (int i = 0; i < lineCount; i++) {
        // Simulate
        val value = generator.apply(i * 1.0f / lineCount);

        // Write
        mapper.writeValue(writer, value);
        writer.println();
      }
    }
  }

  private static void convertJsonFile(String inputFile, String outputFile) {
    JsonParquetConverter.convertJsonFile(inputFile, outputFile);
  }

  private Object createValue(float fraction) {
    return new Quote(DateTime.now(), "RB", fraction * 40, fraction * 30);
  }

  private static ObjectMapper createMapper() {
    return new ObjectMapper().configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
  }

}

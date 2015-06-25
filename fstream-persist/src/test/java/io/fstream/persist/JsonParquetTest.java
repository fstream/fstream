/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist;

import static org.apache.commons.io.FileUtils.deleteQuietly;
import io.fstream.core.model.event.TickEvent;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import lombok.val;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
import org.joda.time.DateTime;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonParquetTest {

  @Test
  public void testJsonParquet() throws Exception {
    // I/O
    val inputFile = "build/data.json";
    val outputFile = "build/data.parquet";

    // Clean
    deleteQuietly(new File(inputFile));
    deleteQuietly(new File(outputFile));

    // Write
    writeJsonFile(inputFile);

    // Read
    val input = readJsonFile(inputFile);

    // Convert
    input.saveAsParquetFile(outputFile);
  }

  private void writeJsonFile(String inputFile) throws IOException {
    try (val writer = new PrintWriter(inputFile)) {
      val mapper = createMapper();

      int count = 100;
      for (int i = 0; i < count; i++) {
        // Simulate
        val value = createValue(i, count);
        mapper.writeValue(writer, value);
        writer.println();
      }
    }
  }

  private DataFrame readJsonFile(String inputFile) {
    val sqlContext = createSQLContext();
    return sqlContext.jsonFile(inputFile);
  }

  private Object createValue(int i, int count) {
    return new TickEvent(DateTime.now(), "RB", i * 40 / count, i * 30 / count);
  }

  private static ObjectMapper createMapper() {
    return new ObjectMapper().configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
  }

  private static SQLContext createSQLContext() {
    val sparkContext = new JavaSparkContext("local", "test");
    return new SQLContext(sparkContext);
  }
}

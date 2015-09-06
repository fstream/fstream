/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist.parquet;

import static org.apache.commons.io.FileUtils.deleteQuietly;
import io.fstream.persist.parquet.JsonParquetConverter;

import java.io.File;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class JsonParquetTest {

  /**
   * Test data.
   */
  String inputFile = "build/data.json";
  String outputFile = "build/data.parquet";

  @Before
  public void setUp() {
    deleteQuietly(new File(outputFile));
  }

  @Test
  public void testConverJsonFile() throws Exception {
    JsonParquetConverter.convertJsonFile(inputFile, outputFile);
  }

}

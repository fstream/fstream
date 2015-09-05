/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist.util;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.hadoop.conf.Configuration;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;

@Slf4j
public class JsonParquetConverter {

  public static void convertJsonFile(String inputFile, String outputFile) {
    val sqlContext = createSQLContext();
    try {
      log.info("Reading and inferring schema '{}'...", inputFile);
      val input = readJsonFile(sqlContext, inputFile);
      log.info("Finished reading");

      log.info("Writing to parquet '{}'...", outputFile);
      writeParquetFile(outputFile, input);
      log.info("Finished writing");
    } finally {
      log.info("Stopping...");
      sqlContext.sparkContext().stop();
    }
  }

  private static DataFrame readJsonFile(SQLContext sqlContext, String inputFile) {
    return sqlContext.read().json(inputFile);
  }

  private static void writeParquetFile(String outputFile, DataFrame input) {
    // Force 1 file
    input.repartition(1).write().parquet(outputFile);
  }

  private static SQLContext createSQLContext() {
    val sparkContext = createSparkContext();

    return new SQLContext(sparkContext);
  }

  private static JavaSparkContext createSparkContext() {
    // Single threaded to avoid multiple reads
    val sparkContext = new JavaSparkContext("local", JsonParquetConverter.class.getName());
    setConfiguration(sparkContext.hadoopConfiguration());

    return sparkContext;
  }

  private static void setConfiguration(Configuration conf) {
    // TODO: https://spark.apache.org/docs/latest/sql-programming-guide.html#configuration
    // Only works works with HDFS file system:
    val ONE_GB = 1024 * 1024 * 1024;
    conf.setInt("dfs.blocksize", ONE_GB);
    conf.setInt("parquet.block.size", ONE_GB);
  }

}

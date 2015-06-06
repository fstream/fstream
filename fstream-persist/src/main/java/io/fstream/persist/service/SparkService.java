/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist.service;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;

import javax.annotation.PostConstruct;

import lombok.Cleanup;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Service responsible for persisting to the long-term HDFS backing store.
 * <p>
 * This class is <em>not</em> thread-safe.
 */
@Slf4j
@Service
@Profile("spark")
public class SparkService {

  @Autowired
  private JavaSparkContext sparkContext;
  @Autowired
  private FileSystem fileSystem;

  @PostConstruct
  public void run() throws IOException {
    val workDir = "/fstream";

    log.info("Deleting work dir '{}'...", workDir);
    fileSystem.delete(new Path(workDir), true);

    val interval = SECONDS.toMillis(30);

    // Contexts
    @Cleanup
    val streamingContext = new JavaStreamingContext(sparkContext, new Duration(interval));
    val sqlContext = new SQLContext(sparkContext);

    val input = streamingContext.textFileStream(workDir);
    input.foreachRDD((rdd, time) -> {
      val schemaRdd = sqlContext.jsonRDD(rdd);
      schemaRdd.saveAsParquetFile(workDir + "/data-" + time.milliseconds());

      return null;
    });

    streamingContext.start();
    streamingContext.awaitTermination();
  }

}

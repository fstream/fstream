/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist.service;

import java.io.IOException;

import javax.annotation.PostConstruct;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;

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
    val path = new Path("/tmp/test.txt");
    fileSystem.delete(path, true);

    log.info("Running!");
    val rdd = sparkContext.parallelize(ImmutableList.of(1, 2, 3));
    rdd.saveAsTextFile(path.toString());
    log.info("Count: {}", rdd.count());
  }

}

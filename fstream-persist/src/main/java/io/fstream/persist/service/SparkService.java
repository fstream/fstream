/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist.service;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.apache.spark.api.java.JavaSparkContext;
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

  @PostConstruct
  public void run() {
    log.info("Running!");
  }

}

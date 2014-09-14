/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.bolt;

import lombok.extern.slf4j.Slf4j;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.tuple.Tuple;

@Slf4j
public class LoggingBolt extends AbstractBasicBolt {

  @Override
  public void execute(Tuple tuple, BasicOutputCollector collector) {
    log.info("***** Tuple: {}", tuple);
  }

}

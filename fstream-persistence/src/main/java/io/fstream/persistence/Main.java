/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persistence;

import io.fstream.persistence.hbase.KafkaConsumer;
import lombok.extern.slf4j.Slf4j;


/**
 * entry point for kafka-consumer
 */
//TODO springify it later
@Slf4j
public class Main {

  public static void main (String[] args) {
    KafkaConsumer kafkaconsumer = new KafkaConsumer();
    kafkaconsumer.run();
  }

}

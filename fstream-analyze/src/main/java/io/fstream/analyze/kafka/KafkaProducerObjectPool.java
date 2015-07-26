/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.analyze.kafka;

import io.fstream.analyze.util.LazySerializableObjectPool;

import java.util.Map;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

@RequiredArgsConstructor
public class KafkaProducerObjectPool extends LazySerializableObjectPool<KafkaProducer> {

  /**
   * Configuration.
   */
  @NonNull
  private final Map<String, String> producerProperties;

  @Override
  protected ObjectPool<KafkaProducer> createDelegate() {
    val pooledObjectFactory = new KafkaProducerPooledObjectFactory(producerProperties);

    val maxNumProducers = 10;
    val poolConfig = new GenericObjectPoolConfig();
    poolConfig.setMaxTotal(maxNumProducers);
    poolConfig.setMaxIdle(maxNumProducers);

    return new GenericObjectPool<KafkaProducer>(pooledObjectFactory, poolConfig);
  }

}
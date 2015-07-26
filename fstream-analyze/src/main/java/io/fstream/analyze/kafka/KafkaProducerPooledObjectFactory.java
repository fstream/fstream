/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.analyze.kafka;

import java.io.Serializable;
import java.util.Map;
import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

@RequiredArgsConstructor
public class KafkaProducerPooledObjectFactory extends BasePooledObjectFactory<KafkaProducer> implements
    Serializable {

  /**
   * Configuration.
   */
  @NonNull
  private final Map<String, String> producerProperties;

  @Override
  public KafkaProducer create() throws Exception {
    val producerConfig = createProducerConfig();

    return new KafkaProducer(new Producer<String, String>(producerConfig));
  }

  @Override
  public PooledObject<KafkaProducer> wrap(KafkaProducer producer) {
    return new DefaultPooledObject<KafkaProducer>(producer);
  }

  @Override
  public void destroyObject(PooledObject<KafkaProducer> pooledObject) throws Exception {
    pooledObject.getObject().close();
    super.destroyObject(pooledObject);
  }

  private ProducerConfig createProducerConfig() {
    val properties = new Properties();
    properties.putAll(producerProperties);

    return new ProducerConfig(properties);
  }

}

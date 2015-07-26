/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.analyze.kafka;

import io.fstream.core.util.Codec;

import java.io.Closeable;
import java.io.IOException;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class KafkaProducer implements Closeable {

  /**
   * Dependencies.
   */
  @NonNull
  private final Producer<String, String> producer;

  public void send(Object payload) {
    String key = "1";
    String value = Codec.encodeText(payload);
    String topic = "results";
    val message = new KeyedMessage<String, String>(topic, key, value);

    producer.send(message);
  }

  @Override
  public void close() throws IOException {
    producer.close();
  }

}

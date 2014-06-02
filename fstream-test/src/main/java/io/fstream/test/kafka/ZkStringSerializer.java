/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.test.kafka;

import kafka.utils.ZKStringSerializer;

import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;

public class ZkStringSerializer implements ZkSerializer {

  @Override
  public byte[] serialize(Object o) throws ZkMarshallingError {
    return ZKStringSerializer.serialize(o);
  }

  @Override
  public Object deserialize(byte[] bytes) throws ZkMarshallingError {
    return ZKStringSerializer.deserialize(bytes);
  }
}
/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.esper;

import java.io.Serializable;

import backtype.storm.tuple.Fields;

public final class EventTypeDescriptor implements Serializable
{

  private final String name;
  private final Fields fields;
  private final String streamId;

  EventTypeDescriptor(String name, String[] fields, String streamId)
  {
    this.name = name;
    this.fields = new Fields(fields);
    this.streamId = streamId;
  }

  public String getName()
  {
    return name;
  }

  public Fields getFields()
  {
    return fields;
  }

  public String getStreamId()
  {
    return streamId;
  }
}
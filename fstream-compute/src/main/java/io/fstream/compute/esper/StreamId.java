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

import lombok.Value;

@Value
public class StreamId implements Serializable {

  private final String componentId;
  private final String streamId;

  public StreamId(String componentId) {
    this(componentId, "default");
  }

  public StreamId(String componentId, String streamId) {
    this.componentId = componentId;
    this.streamId = streamId;
  }

}
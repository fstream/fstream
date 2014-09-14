/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.storm;

import lombok.NonNull;
import lombok.Value;
import backtype.storm.Config;
import backtype.storm.generated.StormTopology;

/**
 * A logical job to run on Storm in the form of a {@link Topology} and {@link Config} pair.
 */
@Value
public class StormJob {

  /**
   * State.
   */
  @NonNull
  private final String id;
  @NonNull
  private final Config config;
  @NonNull
  private final StormTopology topology;

}

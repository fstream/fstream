/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.model;

import backtype.storm.Config;
import backtype.storm.generated.StormTopology;

public interface StormJob {

  Config getConfig();

  StormTopology getTopology();

}

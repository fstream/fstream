/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.core.model.state;

import static com.google.common.collect.Lists.newArrayList;
import io.fstream.core.model.definition.Alert;
import io.fstream.core.model.definition.Metric;

import java.util.List;

import lombok.Data;

@Data
public class State {

  private List<String> symbols = newArrayList();
  private List<String> statements = newArrayList();
  private List<Alert> alerts = newArrayList();
  private List<Metric> metrics = newArrayList();

}

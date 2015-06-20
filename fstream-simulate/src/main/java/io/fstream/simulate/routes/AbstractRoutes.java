/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package io.fstream.simulate.routes;

import static java.util.Collections.disjoint;

import java.util.List;

import lombok.NonNull;
import lombok.val;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ValueBuilder;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.collect.ImmutableList;

public abstract class AbstractRoutes extends RouteBuilder {

  /**
   * Configuration.
   */
  @Value("#{environment.getActiveProfiles()}")
  private List<String> activeProfiles;

  @NonNull
  protected ValueBuilder profilesActive(String... profiles) {
    val active = containsAny(activeProfiles, profiles);
    return constant(active);
  }

  private boolean containsAny(List<String> list, String[] values) {
    return !disjoint(list, ImmutableList.copyOf(values));
  }

}
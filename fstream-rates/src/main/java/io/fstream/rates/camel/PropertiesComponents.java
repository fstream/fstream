/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.camel;

import static com.google.common.collect.Lists.newArrayList;
import static lombok.AccessLevel.PRIVATE;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;

import org.apache.camel.component.properties.PropertiesComponent;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * Utilities for working with {@link PropertiesComponent}s.
 */
@NoArgsConstructor(access = PRIVATE)
public final class PropertiesComponents {

  /**
   * Bridges Spring properties to Camel properties.
   * 
   * @param environment the Spring environment
   * @return
   */
  public static PropertiesComponent newPropertiesComponent(@NonNull Environment environment) {
    val configuration = (ConfigurableEnvironment) environment;
    val propertySources = configuration.getPropertySources();

    val names = newArrayList();
    for (val propertySource : propertySources) {
      names.add(propertySource.getName());
    }

    val component = new PropertiesComponent(names.toArray(new String[names.size()]));
    component.setPropertiesResolver(new EnvironmentPropertiesResolver(environment));

    return component;
  }

}
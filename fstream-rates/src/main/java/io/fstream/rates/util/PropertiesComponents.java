/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.util;

import static com.google.common.collect.Lists.newArrayList;
import static lombok.AccessLevel.PRIVATE;

import java.util.Properties;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import org.apache.camel.CamelContext;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.component.properties.PropertiesResolver;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

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
    component.setPropertiesResolver(new SpringPropertiesResolver(propertySources));

    return component;
  }

  @RequiredArgsConstructor
  private static final class SpringPropertiesResolver implements PropertiesResolver {

    @NonNull
    private final MutablePropertySources propertySources;

    @Override
    public Properties resolveProperties(CamelContext context, boolean ignoreMissingLocation, String... names)
        throws Exception {
      val properties = new Properties();

      // Add in reverse order to preserve precedence.
      for (int i = names.length - 1; i >= 0; i--) {
        val propertySource = propertySources.get(names[i]);
        if (propertySource instanceof EnumerablePropertySource) {
          val propertyNames = ((EnumerablePropertySource<?>) propertySource).getPropertyNames();

          for (val propertyName : propertyNames) {
            properties.put(propertyName, propertySource.getProperty(propertyName));
          }
        }
      }

      return properties;
    }

  }

}

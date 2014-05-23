/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.camel;

import java.util.Properties;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.apache.camel.CamelContext;
import org.apache.camel.component.properties.PropertiesResolver;
import org.springframework.core.env.Environment;

@RequiredArgsConstructor
public final class EnvironmentPropertiesResolver implements PropertiesResolver {

  @NonNull
  private final Environment environment;

  @Override
  public Properties resolveProperties(CamelContext context, boolean ignoreMissingLocation, String... names)
      throws Exception {
    return new Properties() {

      @Override
      public String getProperty(String key) {
        return environment.getProperty(key);
      }

      @Override
      public String getProperty(String key, String defaultValue) {
        return environment.getProperty(key, defaultValue);
      }

    };
  }

}
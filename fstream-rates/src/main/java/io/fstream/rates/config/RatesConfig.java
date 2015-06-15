/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.config;

import io.fstream.core.config.CoreConfig;

import java.util.concurrent.TimeUnit;

import lombok.val;

import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;

@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties
public class RatesConfig extends CoreConfig {

  /**
   * Override the {@link MetricRegistry} installed by Spring Boot's actuator auto-config.
   */
  @Bean
  public MetricRegistry metricRegistry() {
    val registry = new MetricRegistry();
    val reporter = Slf4jReporter.forRegistry(registry)
        .outputTo(LoggerFactory.getLogger("metrics"))
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build();

    reporter.start(60, TimeUnit.SECONDS);

    return registry;
  }

}

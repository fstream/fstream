/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.config;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import lombok.val;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;

/**
 * Metrics configuration.
 */
@Configuration
public class MetricsConfig {

  /**
   * Override the {@link MetricRegistry} installed by Spring Boot's actuator auto-config.
   */
  @Bean
  public MetricRegistry metricRegistry() {
    val registry = new MetricRegistry();
    val reporter = Slf4jReporter.forRegistry(registry)
        .outputTo(LoggerFactory.getLogger("metrics"))
        .convertRatesTo(SECONDS)
        .convertDurationsTo(MILLISECONDS)
        .build();

    reporter.start(10, SECONDS);

    return registry;
  }

}

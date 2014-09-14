/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.config;

import io.fstream.compute.storm.DistributedStormJobExecutor;
import io.fstream.compute.storm.LocalStormJobExecutor;
import io.fstream.compute.storm.StormJobExecutor;
import io.fstream.core.config.CoreConfig;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application level configuration.
 */
@Slf4j
@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties
public class ComputeConfig extends CoreConfig {

  @Bean
  @ConditionalOnExpression("${storm.local}")
  public StormJobExecutor localStormExecutor() {
    log.info("Creating local storm executor...");
    return new LocalStormJobExecutor();
  }

  @Bean
  @ConditionalOnExpression("!${storm.local}")
  public StormJobExecutor distributedStormExecutor() {
    log.info("Creating distributed storm executor...");
    return new DistributedStormJobExecutor();
  }

}

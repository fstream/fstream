/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.config;

import io.fstream.compute.storm.DistributedStormExecutor;
import io.fstream.compute.storm.LocalStormExecutor;
import io.fstream.compute.storm.StormExecutor;
import io.fstream.core.config.CoreConfig;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties
public class ComputeConfig extends CoreConfig {

  @Bean
  @ConditionalOnExpression("${storm.local}")
  public StormExecutor localStormExecutor() {
    log.info("Creating local storm executor...");
    return new LocalStormExecutor();
  }

  @Bean
  @ConditionalOnExpression("!${storm.local}")
  public StormExecutor distributedStormExecutor() {
    log.info("Creating distributed storm executor...");
    return new DistributedStormExecutor();
  }

}

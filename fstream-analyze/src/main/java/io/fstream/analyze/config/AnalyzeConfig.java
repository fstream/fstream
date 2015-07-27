/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.analyze.config;

import io.fstream.analyze.core.JobContext;
import io.fstream.analyze.core.JobContextFactory;
import io.fstream.core.config.CoreConfig;

import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnalyzeConfig extends CoreConfig {

  @Bean
  public JobContextFactory jobContextFactory() {
    return new JobContextFactory();
  }

  @Bean
  public JobContext jobContext() {
    return jobContextFactory().create();
  }

  @Bean
  public JavaStreamingContext streamingContext() {
    return jobContext().getStreamingContext();
  }

}

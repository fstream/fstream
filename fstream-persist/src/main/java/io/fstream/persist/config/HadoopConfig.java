package io.fstream.persist.config;

import io.fstream.persist.config.PersistProperties.HadoopProperties;
import io.fstream.persist.util.Configurations;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Hadoop configuration.
 * <p>
 * See annotation documentation for details.
 */
@org.springframework.context.annotation.Configuration
public class HadoopConfig {

  /**
   * Dependencies.
   */
  @Value("#{sparkContext.hadoopConfiguration()}")
  Configuration conf;
  @Autowired
  HadoopProperties hadoop;

  @Bean
  @Primary
  public Configuration hadoopConf() {
    Configurations.setAll(conf, hadoop.getProperties());

    return conf;
  }

  @Bean
  public FileSystem fileSystem() throws IOException {
    return FileSystem.get(hadoopConf());
  }

}

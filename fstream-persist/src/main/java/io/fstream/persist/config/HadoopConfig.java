package io.fstream.persist.config;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

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

  @Bean
  public FileSystem fileSystem(@Value("#{sparkContext.hadoopConfiguration()}") Configuration conf) throws IOException {
    return FileSystem.get(conf);
  }

}

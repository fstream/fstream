/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.schema;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 
 */
@Slf4j
@Configuration
@EnableAutoConfiguration
@ComponentScan
public class FstreamHbaseSchema {

  @Value("${oanda.rates.symbols}")
  private String tablename;

  public static void main(String[] args) {
    new SpringApplicationBuilder()
        .sources(FstreamHbaseSchema.class)
        .run(args);
  }

  @PostConstruct
  private void print() {
    System.out.println(tablename);
  }
}

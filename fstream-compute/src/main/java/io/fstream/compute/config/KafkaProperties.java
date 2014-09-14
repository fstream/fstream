/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.config;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Kafka specific configuration properties.
 */
@Data
@Component
@ConfigurationProperties("kafka")
public class KafkaProperties {

  /**
   * Kafka properties that effect producer behavior.
   */
  private Map<String, String> producerProperties = newHashMap();

}

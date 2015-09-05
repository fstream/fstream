/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.persist;

import io.fstream.core.model.event.Order;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

@Ignore
@Slf4j
public class BeanDefinitionTest {

  @Test
  public void test() {
    val config = new ObjectMapper().getSerializationConfig();
    val type = config.constructType(Order.class);

    val description = config.introspect(type);
    for (val property : description.findProperties()) {
      log.info("Property: {}", property.getName());
    }
  }

}

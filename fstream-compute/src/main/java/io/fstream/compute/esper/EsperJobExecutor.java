/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.esper;

import lombok.SneakyThrows;
import lombok.val;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("esper")
public class EsperJobExecutor {

  @Autowired
  EsperKafkaConsumer consumer;

  @SneakyThrows
  public void execute(EsperJob job) {
    val queue = consumer.getQueue();
    while (true) {
      val event = queue.take();
      job.execute(event);
    }
  }

}

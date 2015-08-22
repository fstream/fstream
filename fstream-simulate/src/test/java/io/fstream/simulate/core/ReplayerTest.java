/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.core;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;

import java.io.File;
import java.util.concurrent.TimeUnit;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.camel.ProducerTemplate;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ReplayerTest {

  @Mock
  ProducerTemplate template;

  @Test
  @Ignore
  public void testLoop() throws Exception {
    doAnswer(invocation -> {
      log.info("{}", invocation.getArguments()[1]);
      return null;
    }).when(template).sendBody(anyString(), anyObject());

    val replayer = new Replayer();
    replayer.setInputDir(new File("src/test/resources/fixtures/replayer"));
    replayer.setLoop(true);
    replayer.setDelay(0);
    replayer.setSpeed(0.1f);
    replayer.setTemplate(template);
    replayer.init();
    replayer.awaitTerminated(1, TimeUnit.HOURS);
  }
}

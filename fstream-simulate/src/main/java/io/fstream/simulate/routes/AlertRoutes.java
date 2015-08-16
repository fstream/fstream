/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
// @formatter:off

package io.fstream.simulate.routes;

import static io.fstream.simulate.routes.PublishRoutes.PUBLISH_ENDPOINT;
import io.fstream.simulate.util.RandomAlertGenerator;
import lombok.val;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

/**
 * Simulation and publication of alerts by using timers.
 * <p>
 * Useful for simulating alerts.
 */
@Component
@Profile("alerts")
public class AlertRoutes extends AbstractRoutes {

  @Override
  public void configure() throws Exception {
    val period = 5000L;
    val ids = ImmutableList.of(1,2,3,4);
    val symbols = ImmutableList.of("RY", "BMO", "RD", "BBM", "HUF");

    from("timer://alert-timer?daemon=false&period=" + period * 1)
      .process(new RandomAlertGenerator(ids, symbols))
      .to(PUBLISH_ENDPOINT);
  }

}
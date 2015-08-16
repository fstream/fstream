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
import io.fstream.simulate.util.RandomMetricGenerator;
import lombok.val;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Simulation and publication of alerts by using timers.
 * <p>
 * Useful for simulating alerts.
 */
@Component
@Profile("metrics")
public class MetricRoutes extends AbstractRoutes {

  @Override
  public void configure() throws Exception {
    val period = 5000L;
    
    from("timer://metrics-timer1?daemon=false&period=" + period * 1)
      .process(new RandomMetricGenerator(10))
      .to(PUBLISH_ENDPOINT);
  }

}
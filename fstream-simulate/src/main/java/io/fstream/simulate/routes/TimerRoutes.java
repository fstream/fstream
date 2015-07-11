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
import io.fstream.simulate.util.RandomQuoteEventGenerator;
import lombok.val;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Simulation and publication of quotes by using timers.
 * <p>
 * Useful for simulating FX quotes.
 */
@Component
@Profile("timer")
public class TimerRoutes extends AbstractRoutes {

  @Override
  public void configure() throws Exception {
    val period = 5000L;
    
    from("timer://quote-event1?daemon=false&period=" + period * 1)
      .process(new RandomQuoteEventGenerator("EUR/USD", 1.2757f, 1.3990f))
      .to(PUBLISH_ENDPOINT);
    from("timer://quote-event2?daemon=false&period=" + period * 2)
      .process(new RandomQuoteEventGenerator("USD/JPY", 93.8675f, 105.4415f))
      .to(PUBLISH_ENDPOINT);
    from("timer://quote-event3?daemon=false&period=" + period * 3)
      .process(new RandomQuoteEventGenerator("GBP/USD", 1.4817f, 1.6997f))
      .to(PUBLISH_ENDPOINT);
    from("timer://quote-event4?daemon=false&period=" + period * 4)
      .process(new RandomQuoteEventGenerator("AUD/USD", 0.8661f, 0.9791f))
      .to(PUBLISH_ENDPOINT);
    from("timer://quote-event5?daemon=false&period=" + period * 5)
      .process(new RandomQuoteEventGenerator("USD/CHF", 0.8701f, 0.9792f))
      .to(PUBLISH_ENDPOINT);
    from("timer://quote-event6?daemon=false&period=" + period * 6)
      .process(new RandomQuoteEventGenerator("USD/CAD", 1.0138f, 1.1279f))
      .to(PUBLISH_ENDPOINT);
    from("timer://quote-event6?daemon=false&period=" + period * 6)
      .process(new RandomQuoteEventGenerator("AUD/NZD", 1.0138f, 1.1279f))
      .to(PUBLISH_ENDPOINT);
    from("timer://quote-event6?daemon=false&period=" + period * 6)
      .process(new RandomQuoteEventGenerator("NZD/USD", 1.0138f, 1.1279f))
      .to(PUBLISH_ENDPOINT);    
  }

}
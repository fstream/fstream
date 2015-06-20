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

import io.fstream.simulate.util.CodecDataFormat;
import io.fstream.simulate.util.RandomTickEventGenerator;
import lombok.val;

import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Simulation and publication of rates.
 * <p>
 * Useful for simulating FX ticks.
 */
@Component
@Profile("rates")
public class RatesRoutes extends AbstractRoutes {

  /**
   * @see http://camel.apache.org/direct.html
   */
  private static final String RATES_ENDPOINT = "direct:rates";

  @Override
  public void configure() throws Exception {
    val period = 5000L;
    
    from("timer://tick-event1?daemon=false&period=" + period * 1)
      .process(new RandomTickEventGenerator("EUR/USD", 1.2757f, 1.3990f))
      .to(RATES_ENDPOINT);
    from("timer://tick-event2?daemon=false&period=" + period * 2)
      .process(new RandomTickEventGenerator("USD/JPY", 93.8675f, 105.4415f))
      .to(RATES_ENDPOINT);
    from("timer://tick-event3?daemon=false&period=" + period * 3)
      .process(new RandomTickEventGenerator("GBP/USD", 1.4817f, 1.6997f))
      .to(RATES_ENDPOINT);
    from("timer://tick-event4?daemon=false&period=" + period * 4)
      .process(new RandomTickEventGenerator("AUD/USD", 0.8661f, 0.9791f))
      .to(RATES_ENDPOINT);
    from("timer://tick-event5?daemon=false&period=" + period * 5)
      .process(new RandomTickEventGenerator("USD/CHF", 0.8701f, 0.9792f))
      .to(RATES_ENDPOINT);
    from("timer://tick-event6?daemon=false&period=" + period * 6)
      .process(new RandomTickEventGenerator("USD/CAD", 1.0138f, 1.1279f))
      .to(RATES_ENDPOINT);
    from("timer://tick-event6?daemon=false&period=" + period * 6)
      .process(new RandomTickEventGenerator("AUD/NZD", 1.0138f, 1.1279f))
      .to(RATES_ENDPOINT);
    from("timer://tick-event6?daemon=false&period=" + period * 6)
      .process(new RandomTickEventGenerator("NZD/USD", 1.0138f, 1.1279f))
      .to(RATES_ENDPOINT);    
    
    from(RATES_ENDPOINT)
      .marshal(new CodecDataFormat())
      .choice()
        .when(profilesActive("console", "file"))
          .transform().simple("${bodyAs(String)}\n") // Add newline for correct output
      .end()
      .choice()
        .when(profilesActive("console"))
          .to("stream:out")
      .end()
      .choice()
        .when(profilesActive("file"))
          .to("file:build?fileName=fstream-simulate-rates.json")
      .end()
      .choice()
        .when(profilesActive("log"))
          .to("log:toq?showExchangePattern=false&showBodyType=false")
      .end()
      .choice()
        .when(profilesActive("kafka"))
          .setHeader(KafkaConstants.PARTITION_KEY, constant("part-0")) // Single partition for now
          .to("{{simulate.rates.uri}}")
      .end()
      .to("metrics:meter:rates"); // Record metrics    
  }

}
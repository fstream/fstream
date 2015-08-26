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

import java.util.List;

import lombok.Value;
import lombok.val;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

/**
 * Simulation and publication of quotes by using timers.
 * <p>
 * Useful for simulating FX quotes.
 */
@Component
@Profile("fx")
public class FxRoutes extends AbstractRoutes {
  
  private List<Instrument> instruments = Lists.newArrayList(
      new Instrument("EUR/USD", 1.2757f, 1.3990f),
      new Instrument("USD/JPY", 93.8675f, 105.4415f),
      new Instrument("GBP/USD", 1.4817f, 1.6997f),
      new Instrument("AUD/USD", 0.8661f, 0.9791f),
      new Instrument("USD/CHF", 0.8701f, 0.9792f),
      new Instrument("USD/CAD", 1.0138f, 1.1279f),
      new Instrument("AUD/NZD", 1.0138f, 1.1279f),
      new Instrument("NZD/USD", 1.0138f, 1.1279f)
     );

  @Override
  public void configure() throws Exception {
    val period = 5000L;
    
    int i = 1;
    for (val instrument : instruments) {
      from("timer://quote-timer-" + i++ +"?daemon=false&period=" + period * 1)
      .process(new RandomQuoteEventGenerator(instrument.getSymbol(), instrument.getMinMid(), instrument.getMaxMid()))
      .to(PUBLISH_ENDPOINT);      
    }
        
  }

  @Value
  public static class Instrument {
    
    String symbol;
    float minMid;
    float maxMid;
    
  }
  
}
/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.routes;

import io.fstream.core.model.Rate;

import java.math.BigDecimal;

import lombok.val;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.joda.time.DateTime;

/**
 * Route definitions for OANDA FIX handling.
 */
public class SampleRoutes extends AbstractFixRoutes {

  @Override
  public void configure() throws Exception {
    // @formatter:off
    
    from("timer://foo?period=1000")
      .process(new Processor() {
        
        int bid = 1;
        int ask = 1;
        
        @Override
        public void process(Exchange exchange) throws Exception {
          val rate = new Rate(new DateTime(), "EUR/USD", new BigDecimal(bid+=1), new BigDecimal(ask+=2));
          
          exchange.getOut().setBody(rate);
        }
        
      })
      .to("esper://events");
      
    from("esper://events?eql=" + 
        "SELECT " +
        "  symbol, " + 
        "  SUM(ask) AS totalAsk, " + 
        "  AVG(bid) AS avgBid, " + 
        "  COUNT(*) AS count " + 
        "FROM " + 
        "  " + Rate.class.getName() + ".win:time_batch(5 sec) " + 
        "GROUP BY " + 
        "  symbol")
      .log("output: '${body.properties}'");
    
    from("esper://events?eql=" + 
        "SELECT " +
        "  CAST(ask, float) / CAST(prior(1, ask), float) AS askPercentChange " + 
        "FROM " + 
        "  " + Rate.class.getName() + "") 
        .log("output: '${body.properties}'");
    
    // @formatter:on
  }
}
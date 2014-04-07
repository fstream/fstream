/* 
 * Copyright (c) 2014 fStream. All Rights Reserved.
 * 
 * Project and contact information: https://bitbucket.org/fstream/fstream
 * 
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package io.fstream.rates;

import static org.apache.camel.builder.PredicateBuilder.and;
import static org.apache.camel.component.quickfixj.QuickfixjEndpoint.EVENT_CATEGORY_KEY;
import static org.apache.camel.component.quickfixj.QuickfixjEndpoint.MESSAGE_TYPE_KEY;
import static org.apache.camel.component.quickfixj.QuickfixjEventCategory.AdminMessageSent;
import static org.apache.camel.component.quickfixj.QuickfixjEventCategory.AppMessageReceived;
import static org.apache.camel.component.quickfixj.QuickfixjEventCategory.SessionLogon;
import static quickfix.field.MsgType.LOGON;
import static quickfix.field.MsgType.MARKET_DATA_SNAPSHOT_FULL_REFRESH;
import io.fstream.rates.handler.PasswordSetter;
import io.fstream.rates.handler.RatesHandler;
import io.fstream.rates.handler.RatesRegistration;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;

@Slf4j
public class Main {

  public static void main(String... args) throws Exception {
    new Main().run();
  }

  public void run() throws Exception {
    log.info("Starting Camel context");
    val context = createContext();
    context.start();

    try {
      log.info("*** Press enter to stop application");
      System.in.read();
    } finally {
      log.info("Stopping Camel context...");
      context.stop();
    }

  }

  private CamelContext createContext() throws Exception {
    val properties = new PropertiesComponent("classpath:fstream.properties");
    val context = new DefaultCamelContext();
    context.addComponent("properties", properties);
    context.addRoutes(createRoutes());

    return context;
  }

  private RouteBuilder createRoutes() {
    val ratesUri = "quickfix:oanda-fxpractice.cfg?sessionID=FIX.4.4:baijud->OANDA/RATES";

    return new RouteBuilder() {

      @Override
      public void configure() throws Exception {
        // On logon request, set password
        from(ratesUri)
            .filter(
                and(header(EVENT_CATEGORY_KEY).isEqualTo(AdminMessageSent),
                    header(MESSAGE_TYPE_KEY).isEqualTo(LOGON)))
            .bean(PasswordSetter.class);

        // On logon response, register for streams
        from(ratesUri)
            .filter(header(EVENT_CATEGORY_KEY).isEqualTo(SessionLogon))
            .bean(new RatesRegistration())
            .to(ratesUri);

        // On rates response, output rates
        from(ratesUri)
            .filter(
                and(header(EVENT_CATEGORY_KEY).isEqualTo(AppMessageReceived),
                    header(MESSAGE_TYPE_KEY).isEqualTo(MARKET_DATA_SNAPSHOT_FULL_REFRESH)))
            .bean(new RatesHandler());
      }

    };
  }

}
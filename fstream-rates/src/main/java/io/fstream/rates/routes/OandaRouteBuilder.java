/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.routes;

import static org.apache.camel.builder.PredicateBuilder.and;
import static org.apache.camel.component.quickfixj.QuickfixjEndpoint.EVENT_CATEGORY_KEY;
import static org.apache.camel.component.quickfixj.QuickfixjEndpoint.MESSAGE_TYPE_KEY;
import static org.apache.camel.component.quickfixj.QuickfixjEventCategory.AdminMessageSent;
import static org.apache.camel.component.quickfixj.QuickfixjEventCategory.AppMessageReceived;
import static org.apache.camel.component.quickfixj.QuickfixjEventCategory.SessionLogon;
import static quickfix.field.MsgType.LOGON;
import static quickfix.field.MsgType.MARKET_DATA_SNAPSHOT_FULL_REFRESH;
import io.fstream.rates.handler.LogonHandler;
import io.fstream.rates.handler.RatesRegistration;

import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.quickfixj.QuickfixjEventCategory;

public class OandaRouteBuilder extends RouteBuilder {

  @Override
  public void configure() throws Exception {

    //
    // On logon request, set password
    //

    from("{{oanda.fxpractice.uri}}")
        .filter(logon())
        .bean(LogonHandler.class);

    //
    // On logon response, register for rates
    //

    from("{{oanda.fxpractice.uri}}")
        .filter(sessionLogon())
        .bean(RatesRegistration.class)
        .to("{{oanda.fxpractice.uri}}");

    //
    // On rates response, output rates
    //
    from("{{oanda.fxpractice.uri}}")
        .filter(marketDataSnapshotFullRefresh())
        .to("{{fstream.broker.uri}}");

  }

  private Predicate logon() {
    return and(eventCategory(AdminMessageSent), messageType(LOGON));
  }

  private Predicate sessionLogon() {
    return eventCategory(SessionLogon);
  }

  private Predicate marketDataSnapshotFullRefresh() {
    return and(eventCategory(AppMessageReceived), messageType(MARKET_DATA_SNAPSHOT_FULL_REFRESH));
  }

  private Predicate eventCategory(QuickfixjEventCategory eventCategory) {
    return header(EVENT_CATEGORY_KEY).isEqualTo(eventCategory);
  }

  private Predicate messageType(String messageType) {
    return header(MESSAGE_TYPE_KEY).isEqualTo(messageType);
  }

}
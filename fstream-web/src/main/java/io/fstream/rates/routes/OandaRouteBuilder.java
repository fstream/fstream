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
import io.fstream.rates.handler.PasswordSetter;
import io.fstream.rates.handler.RatesRegistration;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.apache.camel.builder.RouteBuilder;

@RequiredArgsConstructor
public class OandaRouteBuilder extends RouteBuilder {

  @NonNull
  private final String ratesUri;

  @Override
  public void configure() throws Exception {

    //
    // On logon request, set password
    //

    from(ratesUri)
        .filter(
            and(header(EVENT_CATEGORY_KEY).isEqualTo(AdminMessageSent),
                header(MESSAGE_TYPE_KEY).isEqualTo(LOGON)))
        .bean(PasswordSetter.class);

    //
    // On logon response, register for streams
    //

    from(ratesUri)
        .filter(header(EVENT_CATEGORY_KEY).isEqualTo(SessionLogon))
        .bean(RatesRegistration.class)
        .to(ratesUri);

    //
    // On rates response, output rates
    //
    from(ratesUri)
        .filter(
            and(header(EVENT_CATEGORY_KEY).isEqualTo(AppMessageReceived),
                header(MESSAGE_TYPE_KEY).isEqualTo(MARKET_DATA_SNAPSHOT_FULL_REFRESH)))
        .bean(RatesRegistration.class);
  }

}
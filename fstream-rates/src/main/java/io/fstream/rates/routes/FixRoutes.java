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

import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.quickfixj.QuickfixjEventCategory;

/**
 * Convenience class for FIX providers.
 */
public abstract class FixRoutes extends RouteBuilder {

  protected Predicate logon() {
    return and(eventCategory(AdminMessageSent), messageType(LOGON));
  }

  protected Predicate sessionLogon() {
    return eventCategory(SessionLogon);
  }

  protected Predicate marketDataSnapshotFullRefresh() {
    return and(eventCategory(AppMessageReceived), messageType(MARKET_DATA_SNAPSHOT_FULL_REFRESH));
  }

  protected Predicate eventCategory(QuickfixjEventCategory eventCategory) {
    return header(EVENT_CATEGORY_KEY).isEqualTo(eventCategory);
  }

  protected Predicate messageType(String messageType) {
    return header(MESSAGE_TYPE_KEY).isEqualTo(messageType);
  }

}
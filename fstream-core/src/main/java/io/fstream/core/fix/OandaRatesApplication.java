/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package io.fstream.core.fix;

import static io.fstream.core.fix.Main.FIX_PASSWORD;
import static quickfix.field.MDEntryType.BID;
import static quickfix.field.MDEntryType.OFFER;
import static quickfix.field.MDUpdateType.FULL_REFRESH;
import static quickfix.field.MsgType.FIELD;
import static quickfix.field.MsgType.LOGON;
import static quickfix.field.SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import quickfix.Application;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.MessageCracker;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.MDUpdateType;
import quickfix.field.MarketDepth;
import quickfix.field.Password;
import quickfix.field.ResetSeqNumFlag;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.field.TargetSubID;
import quickfix.fix44.MarketDataRequest;
import quickfix.fix44.MarketDataSnapshotFullRefresh;

@Slf4j
public class OandaRatesApplication extends MessageCracker implements Application {

  /**
   * Required for rates.
   * <p>
   * @see https://github.com/oanda/fixapidocs/blob/master/oanda-fix-api-msgflow.rst#market-data-connections
   */
  public static final String RATES_SUB_ID = "RATES";

  @Override
  @SneakyThrows
  public void toAdmin(Message message, SessionID sessionID) {
    // See http://www.quickfixj.org/confluence/display/qfj/User+FAQ
    log.info("toAdmin - Message: {}", formatMessage(message, sessionID));
    val msgType = message.getHeader().getString(FIELD);
    if (LOGON.compareTo(msgType) == 0) {
      message.setField(new TargetSubID(RATES_SUB_ID));
      message.setField(new Password(FIX_PASSWORD));
      message.setField(new ResetSeqNumFlag(true));
    }
  }

  @Override
  public void toApp(Message message, SessionID sessionID) throws DoNotSend {
    log.info("toApp - Message: {}", formatMessage(message, sessionID));
  }

  @Override
  @SneakyThrows
  public void fromAdmin(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat,
      IncorrectTagValue, RejectLogon {
    log.info("fromAdmin - Message: {}", formatMessage(message, sessionID));
  }

  @Override
  public void fromApp(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat,
      IncorrectTagValue, UnsupportedMessageType {
    log.info("fromApp - Message: {}", message);
    crack(message, sessionID);
  }

  @Override
  public void onCreate(SessionID sessionID) {
    log.info("onCreate - sessionId: {}", sessionID);
  }

  @Override
  public void onLogon(SessionID sessionID) {
    log.info("onLogon - sessionId: {}", sessionID);
    register(sessionID);
  }

  @Override
  public void onLogout(SessionID sessionID) {
    log.info("onLogout - sessionId: {}", sessionID);
  }

  @Override
  public void onMessage(Message unknown, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
      IncorrectTagValue {
    log.info("onMessage - [unknown]: {}", formatMessage(unknown, sessionID));
  }

  public void onMessage(MarketDataSnapshotFullRefresh refresh, SessionID sessionID) {
    log.info("onMessage - MarketDataSnapshotFullRefresh: {}", formatMessage(refresh, sessionID));
  }

  @SneakyThrows
  private static void register(SessionID sessionID) {
    // All these fields are required
    val message = new MarketDataRequest(
        new MDReqID("fstream-rates"),
        new SubscriptionRequestType(SNAPSHOT_PLUS_UPDATES),
        new MarketDepth(1));

    message.set(new MDUpdateType(FULL_REFRESH));

    // Required for all OANDA rates requests
    message.getHeader().setField(new TargetSubID(RATES_SUB_ID));

    // Entry types
    val entryTypes = new MarketDataRequest.NoMDEntryTypes();
    entryTypes.set(new MDEntryType(BID));
    message.addGroup(entryTypes);
    entryTypes.set(new MDEntryType(OFFER));
    message.addGroup(entryTypes);

    // Symbols
    val relatedSymbols = new MarketDataRequest.NoRelatedSym();
    relatedSymbols.set(new Symbol("EUR/USD"));
    message.addGroup(relatedSymbols);

    log.info("Registering...");
    Session.sendToTarget(message, sessionID);
  }

  @SneakyThrows
  private static String formatMessage(Message message, SessionID sessionID) {
    val session = Session.lookupSession(sessionID);
    val dataDictionary = session.getDataDictionary();
    val xml = message.toXML(dataDictionary);

    return xml;
  }

}

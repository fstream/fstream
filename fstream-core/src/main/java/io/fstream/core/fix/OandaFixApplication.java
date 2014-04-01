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
import quickfix.field.NoMDEntryTypes;
import quickfix.field.NoRelatedSym;
import quickfix.field.Password;
import quickfix.field.ResetSeqNumFlag;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.field.TargetSubID;
import quickfix.fix44.MarketDataSnapshotFullRefresh;
import quickfix.fix44.News;

@Slf4j
public class OandaFixApplication extends MessageCracker implements Application {

  /**
   * Required for rates.
   * <p>
   * @see https://github.com/oanda/fixapidocs/blob/master/oanda-fix-api-msgflow.rst#market-data-connections
   */
  private static final String RATES_SUB_ID = "RATES";

  @Override
  @SneakyThrows
  public void toAdmin(Message message, SessionID sessionID) {
    // See http://www.quickfixj.org/confluence/display/qfj/User+FAQ
    log.info("toAdmin - message: {}", formatMessage(message, sessionID));
    val msgType = message.getHeader().getString(FIELD);
    if (LOGON.compareTo(msgType) == 0) {
      message.setField(new TargetSubID(RATES_SUB_ID));
      message.setField(new Password(FIX_PASSWORD));
      message.setField(new ResetSeqNumFlag(true));
    }
  }

  @Override
  public void toApp(Message message, SessionID sessionID) throws DoNotSend {
    log.info("toApp - message: {}", formatMessage(message, sessionID));
  }

  @Override
  @SneakyThrows
  public void fromAdmin(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat,
      IncorrectTagValue, RejectLogon {
    log.info("fromAdmin - message: {}", formatMessage(message, sessionID));
  }

  @Override
  public void fromApp(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat,
      IncorrectTagValue, UnsupportedMessageType {
    log.info("fromApp - message: {}", formatMessage(message, sessionID));
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

  public void onMessage(News news, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
    log.info("onMessage - news: {}", formatMessage(news, sessionID));
  }

  @Override
  public void onMessage(Message message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
      IncorrectTagValue {
    log.info("onMessage - message: {}", formatMessage(message, sessionID));
  }

  @SneakyThrows
  private static void register(SessionID sessionID) {
    val message = new MarketDataSnapshotFullRefresh();

    val header = message.getHeader();
    header.setField(new SubscriptionRequestType(SNAPSHOT_PLUS_UPDATES));
    header.setField(new TargetSubID(RATES_SUB_ID));
    header.setField(new NoMDEntryTypes(1));
    header.setField(new MDEntryType(BID));
    header.setField(new NoRelatedSym(1));
    header.setField(new Symbol("EUR/USD"));

    log.info("Registering rates: {}", formatMessage(message, sessionID));
    Session.sendToTarget(message, sessionID);
  }

  @SuppressWarnings("unused")
  private static MarketDataSnapshotFullRefresh.NoMDEntries newOfferGroup() {
    val offer = new MarketDataSnapshotFullRefresh.NoMDEntries();
    offer.set(new MDEntryType(OFFER));

    return offer;
  }

  @SuppressWarnings("unused")
  private static MarketDataSnapshotFullRefresh.NoMDEntries newBidGroup() {
    val bid = new MarketDataSnapshotFullRefresh.NoMDEntries();
    bid.set(new MDEntryType(BID));

    return bid;
  }

  private static String formatMessage(Message message, SessionID sessionID) {
    val session = Session.lookupSession(sessionID);
    val dataDictionary = session.getDataDictionary();

    return message.toXML(dataDictionary);
  }

}

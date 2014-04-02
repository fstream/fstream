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
import static quickfix.field.MsgType.FIELD;
import static quickfix.field.MsgType.LOGON;
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
import quickfix.field.Password;
import quickfix.field.ResetSeqNumFlag;
import quickfix.field.TargetSubID;
import quickfix.fix44.News;

@Slf4j
public class OandaFixApplication extends MessageCracker implements Application {

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
    log.info("fromApp - message: {}", message);
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
    log.info("onMessage - news: {}", news);
  }

  @Override
  public void onMessage(Message message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
      IncorrectTagValue {
    log.info("onMessage - message: {}", formatMessage(message, sessionID));
  }

  @SneakyThrows
  private static void register(SessionID sessionID) {
    // val message = Messages.message1();
    // val message = Messages.message2();
    // val message = Messages.message3();
    val message = Messages.message4();

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

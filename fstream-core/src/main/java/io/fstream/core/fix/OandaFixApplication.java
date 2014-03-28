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
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;
import quickfix.field.Password;
import quickfix.field.ResetSeqNumFlag;
import quickfix.fix44.Logon;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.SecurityDefinition;

@Slf4j
public class OandaFixApplication extends MessageCracker implements Application {

  @Override
  @SneakyThrows
  public void toAdmin(Message message, SessionID sessionId) {
    // See http://www.quickfixj.org/confluence/display/qfj/User+FAQ
    log.info("toAdmin - message: {}", message);
    val msgType = message.getHeader().getString(FIELD);
    if (LOGON.compareTo(msgType) == 0) {
      message.setField(new Password(FIX_PASSWORD));
      message.setField(new ResetSeqNumFlag(true));
    }
  }

  @Override
  public void toApp(Message message, SessionID sessionID) throws DoNotSend {
    log.info("toApp - message: {}", message);
  }

  @Override
  public void fromAdmin(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat,
      IncorrectTagValue, RejectLogon {
    log.info("fromAdmin - message: {}", message);
  }

  @Override
  public void fromApp(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat,
      IncorrectTagValue, UnsupportedMessageType {
    log.info("fromApp - message: {}", message);
  }

  @Override
  public void onCreate(SessionID sessionID) {
    log.info("onCreate - sessionId: {}", sessionID);
  }

  @Override
  public void onLogon(SessionID sessionID) {
    log.info("onLogon - sessionId: {}", sessionID);
  }

  @Override
  public void onLogout(SessionID sessionID) {
    log.info("onLogout - sessionId: {}", sessionID);
  }

  public void onMessage(NewOrderSingle newOrderSingle, SessionID sessionID) throws FieldNotFound,
      UnsupportedMessageType, IncorrectTagValue {
    log.info("onMessage - newOrderSingle: {}", newOrderSingle);
    super.onMessage(newOrderSingle, sessionID);
  }

  public void onMessage(SecurityDefinition securityDefinition, SessionID sessionID) throws FieldNotFound,
      UnsupportedMessageType, IncorrectTagValue {
    log.info("onMessage - securityDefinition: {}", securityDefinition);
    super.onMessage(securityDefinition, sessionID);
  }

  public void onMessage(Logon logon, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
      IncorrectTagValue {
    log.info("onMessage - logon: {}", logon);
    super.onMessage(logon, sessionID);
  }

}

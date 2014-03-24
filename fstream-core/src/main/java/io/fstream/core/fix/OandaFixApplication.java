/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package io.fstream.core.fix;

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
import quickfix.fix44.Logon;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.SecurityDefinition;

@Slf4j
public class OandaFixApplication extends MessageCracker implements Application {

	@Override
	public void fromAdmin(Message arg0, SessionID arg1) throws FieldNotFound,
			IncorrectDataFormat, IncorrectTagValue, RejectLogon {
		log.info("Successfully called fromAdmin for sessionId: {}", arg0);
	}

	@Override
	public void fromApp(Message arg0, SessionID arg1) throws FieldNotFound,
			IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
		log.info("Successfully called fromApp for sessionId: {}", arg0);
	}

	@Override
	public void onCreate(SessionID arg0) {
		log.info("Successfully called onCreate for sessionId: {}", arg0);
	}

	@Override
	public void onLogon(SessionID arg0) {
		log.info("Successfully logged on for sessionId: {}", arg0);
	}

	@Override
	public void onLogout(SessionID arg0) {
		log.info("Successfully logged out for sessionId : " + arg0);
	}

	@Override
	public void toAdmin(Message message, SessionID sessionId) {
		log.info("Inside toAdmin");
	}

	@Override
	public void toApp(Message arg0, SessionID arg1) throws DoNotSend {
		log.info("Message : {} for sessionid : {}", arg0, arg1);
	}

	public void onMessage(NewOrderSingle message, SessionID sessionID)
			throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
		log.info("Inside onMessage for New Order Single");
		super.onMessage(message, sessionID);
	}

	public void onMessage(SecurityDefinition message, SessionID sessionID)
			throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
		log.info("Inside onMessage for SecurityDefinition");
		super.onMessage(message, sessionID);
	}

	public void onMessage(Logon message, SessionID sessionID)
			throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
		log.info("Inside Logon Message");
		super.onMessage(message, sessionID);
	}

}

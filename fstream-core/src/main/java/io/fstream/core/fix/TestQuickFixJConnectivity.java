/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package io.fstream.core.fix;

import lombok.val;
import lombok.extern.slf4j.Slf4j;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FileStoreFactory;
import quickfix.Message.Header;
import quickfix.SLF4JLogFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;
import quickfix.field.Account;
import quickfix.field.BeginString;
import quickfix.field.Password;
import quickfix.field.ResetSeqNumFlag;
import quickfix.field.Username;
import quickfix.fix44.Logon;

@Slf4j
public class TestQuickFixJConnectivity {
	
	/**
	 * Config.
	 * <p>
	 * Set on the command line / launcher using {@code -Dfix.username=xxx -Dfix.password=yyy}
	 */
	private static final String FIX_USERNAME = System.getProperty("fix.username");
	private static final String FIX_PASSWORD = System.getProperty("fix.password");
	
    public static void main(String... args) {
        SocketInitiator socketInitiator = null;
        try {
            socketInitiator = createSocketInitiator();
            socketInitiator.start();
            
            Thread.sleep(1000);
            val sessionId = socketInitiator.getSessions().get(0);
            log.info("session id " + sessionId);
            sendLogonRequest(sessionId);
            
            int i = 0;
            val maxAttempts = 5;
            do {
                try {
                    log.info("isLoggedOn: {}", socketInitiator.isLoggedOn());
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i++;
            } while ((!socketInitiator.isLoggedOn()) && (i < maxAttempts));
        } catch (Throwable t) {
        	log.error("Exception logging on: ", t);
        } finally {
            if (socketInitiator != null) {
                socketInitiator.stop(true);
            }
        }
    }
	private static SocketInitiator createSocketInitiator() throws ConfigError {
		val application = new OandaFixApplication();
		val sessionSettings = new SessionSettings("quickfix2.cfg");
		val fileStoreFactory = new FileStoreFactory(sessionSettings);
		val logFactory = new SLF4JLogFactory(sessionSettings);
		val messageFactory = new DefaultMessageFactory();
		
		return new SocketInitiator(application, fileStoreFactory, sessionSettings, logFactory, messageFactory);
	}
	
    private static void sendLogonRequest(SessionID sessionId)throws SessionNotFound {
        Logon logon = new Logon();
        Header header = logon.getHeader();
        header.setField(new BeginString("FIX.4.4"));
        logon.setField(new Username(FIX_USERNAME));
        logon.setField(new Password(FIX_PASSWORD));
        logon.setField(new Account(""));
        logon.set(new ResetSeqNumFlag(true));
        log.info("logon request: {}", logon.toString());
        
        val sent = Session.sendToTarget(logon, sessionId);
        log.info("Logon Message Sent : {}", sent);
    }
    
}

/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package io.fstream.core.fix;

import quickfix.*;
import quickfix.Message.Header;
import quickfix.field.Account;
import quickfix.field.BeginString;
import quickfix.field.HeartBtInt;
import quickfix.field.Password;
import quickfix.field.ResetSeqNumFlag;
import quickfix.field.Username;
import quickfix.fix44.Logon;


public class TestQuickFixJConnectivity {
    public static void main(String[] args) {
        SocketInitiator socketInitiator = null;
        try {
            SessionSettings sessionSettings = new SessionSettings("/Users/bdevani/Documents/eclipse-workspace/fstream/fstream-core/src/main/java/io/fstream/core/fix/quickfix2.cfg");
            Application application = new OandaFixApplication();
            FileStoreFactory fileStoreFactory = new FileStoreFactory(sessionSettings);
            FileLogFactory logFactory = new FileLogFactory(sessionSettings);
            MessageFactory messageFactory = new DefaultMessageFactory();
            socketInitiator = new SocketInitiator(application,
                    fileStoreFactory, sessionSettings, logFactory,
                    messageFactory);
            socketInitiator.start();
            Thread.sleep(1000);
            SessionID sessionId = socketInitiator.getSessions().get(0);
            System.out.println("session id " + sessionId);
            sendLogonRequest(sessionId);
            int i = 0;
            System.out.println("Is loggedOn " + socketInitiator.isLoggedOn());
            do {
                try {
                    Thread.sleep(1000);
                    System.out.println("is logged " + socketInitiator.isLoggedOn());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i++;
            } while ((!socketInitiator.isLoggedOn()) && (i < 10));
           
        } catch (ConfigError e) {
            e.printStackTrace();
        } catch (SessionNotFound e) {
           e.printStackTrace();
        } catch (Exception exp) {
            exp.printStackTrace();
        } finally {
            if (socketInitiator != null) {
                socketInitiator.stop(true);
            }
        }
    }
    private static void sendLogonRequest(SessionID sessionId)
            throws SessionNotFound {
        Logon logon = new Logon();
        Header header = logon.getHeader();
        header.setField(new BeginString("FIX.4.4"));
        logon.setField(new Username("xxx"));
        logon.setField(new Password("yyy"));
        logon.setField(new Account(""));
        logon.set(new ResetSeqNumFlag(true));
        System.out.println(logon.toString());
        boolean sent = Session.sendToTarget(logon, sessionId);
        System.out.println("Logon Message Sent : " + sent);
    }
}

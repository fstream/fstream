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
import quickfix.SLF4JLogFactory;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;

@Slf4j
public class Main {

  /**
   * Config.
   * <p>
   * Set on the command line / launcher using {@code -Dfix.username=xxx -Dfix.password=yyy}
   */
  public static final String FIX_USERNAME = System.getProperty("fix.username");
  public static final String FIX_PASSWORD = System.getProperty("fix.password");
  public static final String FIX_CONFIG_FILE = "quickfix-mina.cfg";

  public static void main(String... args) {
    SocketInitiator socketInitiator = null;
    try {
      socketInitiator = createSocketInitiator();
      socketInitiator.start();

      // Press enter to stop application
      System.in.read();
    } catch (Throwable t) {
      log.error("Exception:", t);
    } finally {
      if (socketInitiator != null) {
        socketInitiator.stop(true);
      }
    }
  }

  private static SocketInitiator createSocketInitiator() throws ConfigError {
    val application = new OandaFixApplication();
    val sessionSettings = new SessionSettings(FIX_CONFIG_FILE);
    val fileStoreFactory = new FileStoreFactory(sessionSettings);
    val logFactory = new SLF4JLogFactory(sessionSettings);
    val messageFactory = new DefaultMessageFactory();

    return new SocketInitiator(application, fileStoreFactory, sessionSettings, logFactory, messageFactory);
  }

}

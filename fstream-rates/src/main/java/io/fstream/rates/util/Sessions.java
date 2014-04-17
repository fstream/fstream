/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.util;

import static lombok.AccessLevel.PRIVATE;
import lombok.NoArgsConstructor;
import lombok.val;
import quickfix.Message;
import quickfix.MessageUtils;
import quickfix.Session;
import quickfix.SessionID;

@NoArgsConstructor(access = PRIVATE)
public final class Sessions {

  public static Session getSession(Message message) {
    val session = resolveSession(resolveSessionID(message));
    if (session != null) {
      return session;
    } else {
      return resolveSession(resolveReverseSessionID(message));
    }
  }

  private static Session resolveSession(SessionID sessionID) {
    return sessionID == null ? null : Session.lookupSession(sessionID);
  }

  private static SessionID resolveSessionID(Message message) {
    return message == null ? null : MessageUtils.getSessionID(message);
  }

  private static SessionID resolveReverseSessionID(Message message) {
    return message == null ? null : MessageUtils.getReverseSessionID(message);
  }

}

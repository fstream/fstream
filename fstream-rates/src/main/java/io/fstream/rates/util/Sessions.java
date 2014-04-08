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
import static quickfix.MessageUtils.getReverseSessionID;
import static quickfix.MessageUtils.getSessionID;
import static quickfix.Session.lookupSession;
import lombok.NoArgsConstructor;
import lombok.val;
import quickfix.Message;
import quickfix.Session;

@NoArgsConstructor(access = PRIVATE)
public final class Sessions {

  public static Session getSession(Message message) {
    val session = lookupSession(getSessionID(message));
    if (session != null) {
      return session;
    } else {
      return lookupSession(getReverseSessionID(message));
    }
  }

}

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
import static quickfix.MessageUtils.getSessionID;
import static quickfix.Session.lookupSession;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import quickfix.Message;

@NoArgsConstructor(access = PRIVATE)
public final class Messages {

  @SneakyThrows
  public static String formatMessage(Message message) {
    val sessionID = getSessionID(message);
    val session = lookupSession(sessionID);
    val dataDictionary = session.getDataDictionary();
    val xml = message.toXML(dataDictionary);

    return xml;
  }

}

/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.util;

import static io.fstream.rates.util.Sessions.getSession;
import static lombok.AccessLevel.PRIVATE;
import lombok.NoArgsConstructor;
import lombok.val;
import quickfix.Message;

@NoArgsConstructor(access = PRIVATE)
public final class Messages {

  public static String formatMessage(Message message) {
    val session = getSession(message);
    if (session == null) {
      return "null";
    }

    val dataDictionary = session.getDataDictionary();
    val xml = message.toXML(dataDictionary);

    return xml;
  }

}

/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.handler;

import org.apache.camel.Body;
import org.apache.camel.language.Simple;

import quickfix.field.Password;
import quickfix.field.ResetSeqNumFlag;
import quickfix.fix44.Logon;

public class LogonHandler {

  public void handle(@Body Logon message, @Simple("${properties:oanda.rates.password}") String password) {
    message.setField(new Password(password));
    message.setField(new ResetSeqNumFlag(true));
  }

}
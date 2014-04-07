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

import quickfix.Message;
import quickfix.field.Password;
import quickfix.field.ResetSeqNumFlag;

public class PasswordSetter {

  public void set(@Body Message message, @Simple("${properties:oanda.fxpractice.password}") String password) {
    message.setField(new Password(password));
    message.setField(new ResetSeqNumFlag(true));
  }

}
/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.handler;

import lombok.Setter;
import lombok.val;

import org.apache.camel.CamelExchangeException;
import org.apache.camel.Exchange;
import org.apache.camel.PropertyInject;

import quickfix.Message;
import quickfix.field.Password;
import quickfix.field.ResetSeqNumFlag;

public class PasswordSetter {

  @Setter
  @PropertyInject("oanda.fxpractice.password")
  private String password;

  public void set(Exchange exchange) throws CamelExchangeException {
    val message = exchange.getIn().getMandatoryBody(Message.class);
    message.setField(new Password(password));
    message.setField(new ResetSeqNumFlag(true));
  }

}
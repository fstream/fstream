/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.feed.handler;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.camel.Body;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import quickfix.field.Password;
import quickfix.field.ResetSeqNumFlag;
import quickfix.fix44.Logon;

/**
 * Handler that manages the setting of FIX login credentials.
 */
@Slf4j
@Setter
@Component
public class LogonHandler {

  /**
   * FIX account password.
   */
  @Value("${feed.password}")
  private String password;

  public void handle(@Body Logon message) {
    log.info("Setting password to '<{} characters>' ...", password.length());
    message.setField(new Password(password));
    message.setField(new ResetSeqNumFlag(true));
  }

}
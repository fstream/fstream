/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.handler;

import static io.fstream.rates.fix.Messages.formatMessage;
import lombok.extern.slf4j.Slf4j;

import org.apache.camel.Body;
import org.springframework.stereotype.Component;

import quickfix.fix44.Message;

/**
 * Bean that logs FIX messages.
 */
@Slf4j
@Component
public class FixMessageLogger {

  public void log(@Body Message message) {
    log.info("Message: {}", formatMessage(message));
  }

}
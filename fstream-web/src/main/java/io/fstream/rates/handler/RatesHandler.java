/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.rates.handler;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.camel.Exchange;

import quickfix.fix44.Message;

@Slf4j
public class RatesHandler {

  public void handle(Exchange exchange) {
    // TODO: send to message queue declaratively and remove this processor
    // TODO: stub out https://gist.github.com/mardambey/2650743
    val message = exchange.getIn().getBody(Message.class);
    log.info("Rates: {}", message.toXML());

  }
}
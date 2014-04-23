/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.web.controller;

import io.fstream.web.model.RegisterMessage;
import io.fstream.web.model.Registration;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class RegistrationController {

  @MessageMapping("/register")
  @SendTo("/topic/events")
  public Registration register(RegisterMessage message) throws Exception {
    log.info("Registering '{}'", message);
    return new Registration(message.getInstrument());
  }

}

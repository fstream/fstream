/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.web.controller;

import io.fstream.core.model.definition.Alert;
import io.fstream.core.model.state.State;
import io.fstream.core.service.StateService;
import lombok.Setter;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class RegistrationController {

  /**
   * Dependencies.
   */
  @Setter
  @Autowired
  protected StateService stateService;

  @MessageMapping("/register")
  @SendTo("/topic/state")
  public State register(Alert alert) throws Exception {
    log.info("Registering '{}'", alert);
    val state = stateService.getState();

    val id = getNextId(state);
    alert.setId(id);

    state.getAlerts().add(alert);

    stateService.setState(state);

    return state;
  }

  private int getNextId(State state) {
    // TODO: This is temporary. Ids should be generated randomly
    int max = 0;
    for (val alert : state.getAlerts()) {
      if (alert.getId() > max) {
        max = alert.getId();
      }
    }

    return max + 1;
  }

}

/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.web.service;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ConfigService {

  /**
   * Dependencies.
   */
  // @Setter
  // @Autowired
  // protected StateService stateService;
  @Setter
  @Autowired
  protected SimpMessagingTemplate template;

  // @PostConstruct
  // @SneakyThrows
  public void initialize() {
    // stateService.register(new StateListener() {
    //
    // @Override
    // public void onUpdate(State state) {
    // log.info("Configuration updated: {}", state);
    // template.send("/topic/commands", createMessage(state));
    //
    // }
    //
    // });
  }
  //
  // private static Message<byte[]> createMessage(State state) {
  // return MessageBuilder.withPayload(Codec.encodeBytes(state)).build();
  // }

}

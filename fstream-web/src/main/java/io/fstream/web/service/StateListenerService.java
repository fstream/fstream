/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.web.service;

import io.fstream.core.model.state.State;
import io.fstream.core.model.state.StateListener;
import io.fstream.core.service.StateService;
import io.fstream.core.util.Codec;

import javax.annotation.PostConstruct;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StateListenerService implements StateListener {

  /**
   * Dependencies.
   */
  @NonNull
  private final StateService stateService;
  @NonNull
  private final SimpMessagingTemplate template;

  @PostConstruct
  @SneakyThrows
  public void initialize() {
    stateService.initialize();
    stateService.addListener(this);
  }

  @Override
  public void onUpdate(State state) {
    log.info("State updated: {}", state);
    template.send("/topic/commands", createMessage(state));
  }

  private static Message<byte[]> createMessage(State state) {
    return MessageBuilder.withPayload(Codec.encodeBytes(state)).build();
  }

}

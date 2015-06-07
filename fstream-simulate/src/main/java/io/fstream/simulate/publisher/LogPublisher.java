package io.fstream.simulate.publisher;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!kafka")
public class LogPublisher implements Publisher {

  @Override
  public void publish(Object message) {
    log.info("Event: {}", message);
  }

}

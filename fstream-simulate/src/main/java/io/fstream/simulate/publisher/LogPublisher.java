package io.fstream.simulate.publisher;

import io.fstream.core.util.Codec;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("log")
public class LogPublisher implements Publisher {

  @Override
  public void publish(Object message) {
    log.info("Event: {}", Codec.encodeText(message));
  }

}

package io.fstream.simulate.output;

import io.fstream.core.model.event.Event;
import io.fstream.core.util.Codec;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("log")
public class LogOutput implements Output {

  @Override
  public void write(Event event) {
    log.info("Event: {}", Codec.encodeText(event));
  }

}

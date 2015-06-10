package io.fstream.simulate.publisher;

import io.fstream.core.util.Codec;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("console")
public class ConsolePublisher implements Publisher {

  @Override
  public void publish(Object message) {
    System.out.println(Codec.encodeText(message));
  }

}

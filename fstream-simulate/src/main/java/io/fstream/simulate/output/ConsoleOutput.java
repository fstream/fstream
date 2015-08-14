package io.fstream.simulate.output;

import io.fstream.core.util.Codec;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("console")
public class ConsoleOutput implements Output {

  @Override
  public void write(Object message) {
    System.out.println(Codec.encodeText(message));
  }

}

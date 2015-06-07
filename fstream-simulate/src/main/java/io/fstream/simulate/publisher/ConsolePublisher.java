package io.fstream.simulate.publisher;

import io.fstream.core.util.Codec;

public class ConsolePublisher implements Publisher {

  @Override
  public void publish(Object message) {
    System.out.println(Codec.encodeText(message));
  }

}

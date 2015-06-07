package io.fstream.simulate.publisher;

public class NoOpPublisher implements Publisher {

  @Override
  public void publish(Object message) {
    // No-op
  }

}

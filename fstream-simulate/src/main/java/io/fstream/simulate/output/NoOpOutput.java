package io.fstream.simulate.output;

public class NoOpOutput implements Output {

  @Override
  public void write(Object message) {
    // No-op
  }

}

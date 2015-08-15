package io.fstream.simulate.output;

import io.fstream.core.model.event.Event;

public class NoOpOutput implements Output {

  @Override
  public void write(Event event) {
    // No-op
  }

}

package io.fstream.simulate.output;

import io.fstream.core.model.event.Event;

public interface Output {

  void write(Event event);

}

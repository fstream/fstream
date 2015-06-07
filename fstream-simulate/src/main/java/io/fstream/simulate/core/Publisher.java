package io.fstream.simulate.core;

import io.fstream.core.model.event.Event;

public interface Publisher {

  void publish(Event event);

}

package io.fstream.simulate.core;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import io.fstream.core.model.event.Event;

@Slf4j
@Component
@Profile("!kafka")
public class LogPublisher implements Publisher {

	@Override
	public void publish(Event event) {
		log.info("Event: {}", event);
	}

}

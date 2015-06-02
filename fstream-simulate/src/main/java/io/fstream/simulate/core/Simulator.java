package io.fstream.simulate.core;

import io.fstream.simulate.config.SimulateProperties;

import javax.annotation.PostConstruct;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Data
@Component
public class Simulator {

	/**
	 * Dependencies.
	 */
	@Autowired
	private Akka akka;
	@Autowired
	private SomeOtherComponent other;
	
	/**
	 * Configuration.
	 */
	@Autowired
	private SimulateProperties properties;
	
	@PostConstruct
	public void simulate() {
		log.info("Simulating for {} seconds with instruments {}", properties.getSeconds(), properties.getInstruments());
		akka.start();
	}
	
}

package io.fstream.simulate.config;

import io.fstream.simulate.core.Akka;
import io.fstream.simulate.core.SomeOtherComponent;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimulateConfig {

	@Bean
	public Akka akka() {
		return new Akka(); // Bogus class, but replace this with the main Akka abstraction once constructed.
	}
	
	@Bean
	public SomeOtherComponent other() {
		return new SomeOtherComponent(akka()); // notice the chaining. Will not create another bean, but will reuse!
	}
	
	// Add other singleton beans here.
	
}

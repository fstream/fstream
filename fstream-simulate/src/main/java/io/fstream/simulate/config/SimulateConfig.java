package io.fstream.simulate.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import akka.actor.ActorSystem;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

@Configuration
public class SimulateConfig {

	 	@Autowired
	    private ApplicationContext applicationContext;

	    @Autowired
	    private SpringExtension springExtension;

	    @Bean
	    public ActorSystem actorSystem() {
	        ActorSystem system = ActorSystem
	            .create("tradingApp", akkaConfiguration());
	        springExtension.initialize(applicationContext);
	        return system;
	    }
	   
	    @Bean
	    public Config akkaConfiguration() {
	        return ConfigFactory.load();
	    }
	
	
}

package io.fstream.simulate.config;

import io.fstream.simulate.agents.*;
import io.fstream.simulate.core.ActorFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;

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
	
	    @Bean
	    public ActorRef exchange() {
	    	return actorSystem().actorOf(Props.create(Exchange.class, () -> new Exchange()));
	    }
	    
	    @Bean
	    @Scope("prototype")
	    public ActorRef retailAgent(String name, ActorRef exchange) {
	    	return actorSystem().actorOf(Props.create(RetailAgent.class, () -> new RetailAgent(name, exchange)));
	    }
	    
	    @Bean
	    @Scope("prototype")
	    public ActorRef institutionalAgent(String name, ActorRef exchange) {
	    	return actorSystem().actorOf(Props.create(InstitutionalAgent.class, () -> new InstitutionalAgent(name, exchange)));
	    }
	    
	    @Bean
	    public ActorFactory actorFactory() {
	    	return new ActorFactory() {
				
	    		@Override
	    		public ActorRef createExchange() {
	    			return exchange();
	    		}

	    		@Override
				public ActorRef createRetailAgent(String name) {
					return retailAgent(name, exchange());
				}
				
				@Override
				public ActorRef createInstitutionalAgent(String name) {
					return institutionalAgent(name, exchange());
				}
				
			};
	    }
	
}

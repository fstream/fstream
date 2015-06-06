package io.fstream.simulate.core;

import akka.actor.ActorRef;

public interface ActorFactory {

	ActorRef createExchange();
	ActorRef createRetailAgent(String name);
	ActorRef createInstitutionalAgent(String name);
	
}

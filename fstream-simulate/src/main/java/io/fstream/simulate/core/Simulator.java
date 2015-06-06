package io.fstream.simulate.core;

import static akka.pattern.Patterns.gracefulStop;
import io.fstream.simulate.agents.Exchange;
import io.fstream.simulate.agents.InstitutionalAgent;
import io.fstream.simulate.agents.RetailAgent;
import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.messages.Messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import lombok.Data;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

@Slf4j
@Data
@Component
public class Simulator {

	/**
	 * Configuration.
	 */
	@Autowired
	private SimulateProperties properties;
	
	/**
	 * Dependencies.
	 */
	@Autowired
	private ActorSystem tradingApp;
	@Autowired
	private ActorFactory actorFactory;
	
	@PostConstruct
	public void simulate() {
		log.info("Simulating for {} seconds with instruments {}", properties.getSeconds(), properties.getInstruments());
		//ActorSystem tradingApp = ActorSystem.create("tradingApp");
		
		
		ActorRef exchange = tradingApp.actorOf(Props.create(Exchange.class), "exchange");
		
		val agents = new HashMap<String,List<ActorRef>>();
		agents.put("retail", new ArrayList<ActorRef>());
		for (int i = 0; i < 1000; i++) {
			String name = "ret" + i;
			// val retailActor = tradingApp.actorOf(Props.create(RetailAgent.class,name,exchange), name);
			
			val retailActor = actorFactory.createRetailAgent(name);
			agents.get("retail").add(retailActor);
			//agents.get("retail").add(tradingApp.actorOf(springext.props("retailagent")));
		}
		agents.put("inst", new ArrayList<ActorRef>());
		for (int i = 0; i < 3000; i++) {
			String name = "inst" + i;
			agents.get("inst").add(tradingApp.actorOf(Props.create(InstitutionalAgent.class,name,exchange), name));
		}
		
	
		
		tradingApp.scheduler().scheduleOnce(Duration.create(20,TimeUnit.SECONDS), new Runnable() {
			@Override
			public void run() {	
				log.info("done simulation - initiate shutdown");
				for (Entry<String, List<ActorRef>> agentqueue: agents.entrySet()) {
					for (val agent: agentqueue.getValue()) {
						val stopped = gracefulStop(agent, Duration.create(1,TimeUnit.MILLISECONDS));
						try {
							Await.result(stopped, Duration.create(1,TimeUnit.SECONDS));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}	
				try {
					exchange.tell(Messages.PRINT_SUMMARY, ActorRef.noSender());
					val stopped = gracefulStop(exchange, Duration.create(30,TimeUnit.SECONDS));
					Await.result(stopped, Duration.create(10,TimeUnit.SECONDS));
					log.info("done simulation - shutdown complete");
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				tradingApp.terminate();
			}
		}, tradingApp.dispatcher());
	}
		
		
		
		
	
	
}

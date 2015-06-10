package io.fstream.simulate.core;

import io.fstream.simulate.agent.Exchange;
import io.fstream.simulate.agent.InstitutionalAgent;
import io.fstream.simulate.agent.RetailAgent;
import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.message.Messages;
import io.fstream.simulate.spring.SpringExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;

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
  private SpringExtension spring;

  ActorRef exchange;
  HashMap<String, List<ActorRef>> agents;

  @PostConstruct
  public void simulate() {
    log.info("Simulating for {} seconds with instruments {}", properties.getSeconds(), properties.getInstruments());

    exchange = tradingApp.actorOf(spring.props(Exchange.class), "exchange");

    agents = new HashMap<String, List<ActorRef>>();
    agents.put("retail", new ArrayList<ActorRef>());
    for (int i = 0; i < properties.getRetProp().getNumAgents(); i++) {
      String name = "retailAgent" + i;
      val retailAgent = tradingApp.actorOf(spring.props(RetailAgent.class, name, exchange), name);

      agents.get("retail").add(retailAgent);
    }
    agents.put("inst", new ArrayList<ActorRef>());
    for (int i = 0; i < properties.getInstProp().getNumAgents(); i++) {
      String name = "institutionalAgent" + i;
      val institutionalAgent = tradingApp.actorOf(spring.props(InstitutionalAgent.class, name, exchange), name);
      agents.get("inst").add(institutionalAgent);
    }
    Runtime.getRuntime().addShutdownHook(new Thread() {

      @Override
      public void run() {
        shutdown();
      }
    });
  }

  @PreDestroy
  @SneakyThrows
  public void shutdown() {
    log.info("shutting down actor system");
    for (val agentlist : agents.entrySet()) {
      for (val agent : agentlist.getValue()) {
        agent.tell(PoisonPill.getInstance(), ActorRef.noSender());
      }
    }
    Thread.sleep(5000);
    exchange.tell(Messages.PRINT_SUMMARY, ActorRef.noSender());
    Thread.sleep(5000);
    exchange.tell(PoisonPill.getInstance(), ActorRef.noSender());
    Thread.sleep(5000);
    tradingApp.shutdown();
    log.info("actor system shutdown complete");
  }
}

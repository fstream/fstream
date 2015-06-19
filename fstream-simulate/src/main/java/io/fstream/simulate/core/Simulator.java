package io.fstream.simulate.core;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import io.fstream.simulate.agent.Exchange;
import io.fstream.simulate.agent.InstitutionalAgent;
import io.fstream.simulate.agent.Publisher;
import io.fstream.simulate.agent.RetailAgent;
import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.message.Messages;
import io.fstream.simulate.spring.SpringExtension;

import java.util.List;
import java.util.Map;

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

import com.google.common.collect.ImmutableMap;

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

  /**
   * Actors.
   */
  private ActorRef exchange;
  private ActorRef publisher;
  private Map<String, List<ActorRef>> agents;

  public void simulate() {
    log.info("Simulating for {} seconds with instruments {}", properties.getSeconds(), properties.getInstruments());
    publisher = createPublisher();
    exchange = createExchange();
    agents = createAgents();
  }

  private ActorRef createExchange() {
    val props = spring.props(Exchange.class, publisher);
    return tradingApp.actorOf(props, "exchange");
  }

  private ActorRef createPublisher() {
    val props = spring.props(Publisher.class);
    return tradingApp.actorOf(props, "publisher");
  }

  private Map<String, List<ActorRef>> createAgents() {
    return ImmutableMap.of("retail", createRetailAgents(), "inst", createInstitutionalAgents());
  }

  private List<ActorRef> createRetailAgents() {
    val count = properties.getRetailProp().getNumAgents();
    return range(0, count).mapToObj(this::createRetailAgent).collect(toList());
  }

  private ActorRef createRetailAgent(int i) {
    val name = "retailAgent" + i;
    val props = spring.props(RetailAgent.class, name, exchange);
    return tradingApp.actorOf(props, name);
  }

  private List<ActorRef> createInstitutionalAgents() {
    val count = properties.getInstitutionalProp().getNumAgents();
    return range(0, count).mapToObj(this::createInstitutionalAgent).collect(toList());
  }

  private ActorRef createInstitutionalAgent(int i) {
    val name = "institutionalAgent" + i;
    val props = spring.props(InstitutionalAgent.class, name, exchange);
    return tradingApp.actorOf(props, name);
  }

  @PreDestroy
  @SneakyThrows
  public void shutdown() {
    log.info("Shutting down actor system");
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
    publisher.tell(PoisonPill.getInstance(), ActorRef.noSender());
    Thread.sleep(5000);

    tradingApp.shutdown();
    log.info("Actor system shutdown complete");
  }

}

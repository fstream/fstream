package io.fstream.simulate.core;

import static com.google.common.base.Stopwatch.createStarted;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import io.fstream.simulate.actor.Exchange;
import io.fstream.simulate.actor.Publisher;
import io.fstream.simulate.actor.agent.InstitutionalAgent;
import io.fstream.simulate.actor.agent.RetailAgent;
import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.message.Messages;
import io.fstream.simulate.util.SpringExtension;

import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

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

import com.google.common.base.Stopwatch;
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
   * State.
   */
  private ActorRef exchange;
  private ActorRef publisher;
  private Map<String, List<ActorRef>> agents;

  private Stopwatch watch;

  public void simulate() {
    log.info("Simulating for {} seconds with instruments {}", properties.getSeconds(), properties.getInstruments());
    watch = createStarted();
    publisher = createPublisher();
    exchange = createExchange();
    agents = createAgents();
  }

  private ActorRef createPublisher() {
    val name = "publisher";
    val props = spring.props(Publisher.class);
    return tradingApp.actorOf(props, name);
  }

  private ActorRef createExchange() {
    val name = "exchange";
    val props = spring.props(Exchange.class, publisher);
    return tradingApp.actorOf(props, name);
  }

  private Map<String, List<ActorRef>> createAgents() {
    return ImmutableMap.of("retail", createRetailAgents(), "inst", createInstitutionalAgents());
  }

  private List<ActorRef> createRetailAgents() {
    return createActors(properties.getRetailProp().getNumAgents(), this::createRetailAgent);
  }

  private ActorRef createRetailAgent(int i) {
    val name = "retailAgent" + i;
    val props = spring.props(RetailAgent.class, name, exchange);
    return tradingApp.actorOf(props, name);
  }

  private List<ActorRef> createInstitutionalAgents() {
    return createActors(properties.getInstitutionalProp().getNumAgents(), this::createInstitutionalAgent);
  }

  private ActorRef createInstitutionalAgent(int i) {
    val name = "institutionalAgent" + i;
    val props = spring.props(InstitutionalAgent.class, name, exchange);
    return tradingApp.actorOf(props, name);
  }

  private static List<ActorRef> createActors(int count, IntFunction<ActorRef> factory) {
    return range(0, count).mapToObj(factory).collect(toList());
  }

  @PreDestroy
  @SneakyThrows
  public void shutdown() {
    val shutdownWatch = createStarted();
    log.info("Shutting down actor system after simulating for {}", watch);
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
    log.info("Actor system shutdown complete in {}", shutdownWatch);
  }

}

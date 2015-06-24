package io.fstream.simulate.core;

import static akka.actor.ActorRef.noSender;
import static com.google.common.base.Stopwatch.createStarted;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import io.fstream.simulate.actor.Exchange;
import io.fstream.simulate.actor.Publisher;
import io.fstream.simulate.actor.agent.HFTAgent;
import io.fstream.simulate.actor.agent.InstitutionalAgent;
import io.fstream.simulate.actor.agent.RetailAgent;
import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.message.Messages;
import io.fstream.simulate.util.SpringExtension;

import java.util.List;
import java.util.function.IntFunction;

import javax.annotation.PreDestroy;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;

@Slf4j
@Data
@Component
@Profile("toq")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Simulator {

  /**
   * Dependencies.
   */
  @NonNull
  private final ActorSystem actorSystem;
  @NonNull
  private final SimulateProperties properties;
  @NonNull
  private final SpringExtension spring;

  /**
   * State.
   */
  private ActorRef exchange;
  private ActorRef publisher;
  private List<ActorRef> agents;

  private Stopwatch watch;

  public void simulate() {
    log.info("Simulating continuosly with instruments {}", properties.getInstruments());
    watch = createStarted();
    publisher = createPublisher();
    exchange = createExchange();
    agents = createAgents();
  }

  @PreDestroy
  public void shutdown() throws InterruptedException {
    val shutdownWatch = createStarted();
    log.info("Shutting down actor system after simulating for {}", watch);

    // Agents
    for (val agent : agents) {
      shutdown(agent);
    }
    pause();

    // Exchange
    exchange.tell(Messages.PRINT_SUMMARY, noSender());
    shutdown(exchange);
    pause();

    // Publisher
    shutdown(publisher);
    pause();

    // System
    actorSystem.shutdown();
    log.info("Actor system shutdown complete in {}", shutdownWatch);
  }

  private ActorRef createPublisher() {
    val name = "publisher";
    val props = spring.props(Publisher.class);
    return actorSystem.actorOf(props, name);
  }

  private ActorRef createExchange() {
    val name = "exchange";
    val props = spring.props(Exchange.class, publisher);
    return actorSystem.actorOf(props, name);
  }

  private List<ActorRef> createAgents() {
    return ImmutableList.<ActorRef> builder().addAll(createRetailAgents()).addAll(createInstitutionalAgents())
        .addAll(createHftAgents()).build();
  }

  private List<ActorRef> createRetailAgents() {
    return createActors(properties.getRetail().getNumAgents(), this::createRetailAgent);
  }

  private ActorRef createRetailAgent(int i) {
    val name = "retailAgent-" + i;
    val props = spring.props(RetailAgent.class, name, exchange);
    return actorSystem.actorOf(props, name);
  }

  private List<ActorRef> createInstitutionalAgents() {
    return createActors(properties.getInstitutional().getNumAgents(), this::createInstitutionalAgent);
  }

  private ActorRef createInstitutionalAgent(int i) {
    val name = "institutionalAgent-" + i;
    val props = spring.props(InstitutionalAgent.class, name, exchange);
    return actorSystem.actorOf(props, name);
  }

  private List<ActorRef> createHftAgents() {
    return createActors(properties.getHft().getNumAgents(), this::createHftAgent);
  }

  private ActorRef createHftAgent(int i) {
    val name = "HFTAgent-" + i;
    val props = spring.props(HFTAgent.class, name, exchange);
    return actorSystem.actorOf(props, name);
  }

  private static List<ActorRef> createActors(int count, IntFunction<ActorRef> factory) {
    return range(0, count).mapToObj(factory).collect(toList());
  }

  private static void pause() throws InterruptedException {
    Thread.sleep(5000);
  }

  private static void shutdown(ActorRef actor) {
    actor.tell(PoisonPill.getInstance(), noSender());
  }

}

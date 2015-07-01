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
import io.fstream.simulate.message.Command;

import java.util.List;
import java.util.function.IntFunction;

import javax.annotation.PreDestroy;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;

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
  public void shutdown() {
    val shutdownWatch = createStarted();
    log.info("Shutting down actor system after simulating for {}", watch);

    // Agents
    for (val agent : agents) {
      shutdown(agent);
    }
    pause();

    // Exchange
    exchange.tell(Command.PRINT_SUMMARY, noSender());
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
    val props = Props.create(Publisher.class);
    return actorSystem.actorOf(props, name);
  }

  private ActorRef createExchange() {
    val name = "exchange";
    val props = Props.create(Exchange.class, properties);
    return actorSystem.actorOf(props, name);
  }

  private List<ActorRef> createAgents() {
    return ImmutableList.<ActorRef> builder()
        .addAll(createRetailAgents())
        .addAll(createInstitutionalAgents())
        .addAll(createHftAgents()).build();
  }

  private List<ActorRef> createRetailAgents() {
    log.info("Creating {} retail agents...", properties.getRetail().getNumAgents());
    return createActors(properties.getRetail().getNumAgents(), this::createRetailAgent);
  }

  private ActorRef createRetailAgent(int i) {
    val name = "retailAgent-" + i;
    val props = Props.create(RetailAgent.class, properties, name);
    return actorSystem.actorOf(props, name);
  }

  private List<ActorRef> createInstitutionalAgents() {
    log.info("Creating {} institutional agents...", properties.getInstitutional().getNumAgents());
    return createActors(properties.getInstitutional().getNumAgents(), this::createInstitutionalAgent);
  }

  private ActorRef createInstitutionalAgent(int i) {
    val name = "institutionalAgent-" + i;
    val props = Props.create(InstitutionalAgent.class, properties, name);
    return actorSystem.actorOf(props, name);
  }

  private List<ActorRef> createHftAgents() {
    log.info("Creating {} hft agents...", properties.getHft().getNumAgents());
    return createActors(properties.getHft().getNumAgents(), this::createHftAgent);
  }

  private ActorRef createHftAgent(int i) {
    val name = "hftAgent-" + i;
    val props = Props.create(HFTAgent.class, properties, name);
    return actorSystem.actorOf(props, name);
  }

  private static List<ActorRef> createActors(int count, IntFunction<ActorRef> factory) {
    return range(0, count).mapToObj(factory).collect(toList());
  }

  @SneakyThrows
  private static void pause() {
    Thread.sleep(5000);
  }

  private static void shutdown(ActorRef actor) {
    actor.tell(PoisonPill.getInstance(), noSender());
  }

}

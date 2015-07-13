package io.fstream.simulate.core;

import static akka.actor.ActorRef.noSender;
import static com.google.common.base.Stopwatch.createStarted;
import io.fstream.simulate.actor.Broker;
import io.fstream.simulate.actor.Exchange;
import io.fstream.simulate.actor.Publisher;
import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.message.Command;

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

@Slf4j
@Data
@Component
@Profile("akka")
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
  private ActorRef broker;

  private Stopwatch watch;

  public void simulate() {
    log.info("Simulating continuosly with instruments {}", properties.getInstruments());
    watch = createStarted();
    publisher = createPublisher();
    exchange = createExchange();
    broker = createBroker();
  }

  @PreDestroy
  public void shutdown() {
    val shutdownWatch = createStarted();
    log.info("Shutting down actor system after simulating for {}", watch);

    // Broker
    shutdown(broker);

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

  private ActorRef createBroker() {
    val name = "broker";
    val props = Props.create(Broker.class, properties);
    return actorSystem.actorOf(props, name);
  }

  @SneakyThrows
  private static void pause() {
    Thread.sleep(5000);
  }

  private static void shutdown(ActorRef actor) {
    actor.tell(PoisonPill.getInstance(), noSender());
  }

}

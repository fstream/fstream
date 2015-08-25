/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.actor;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import io.fstream.simulate.actor.agent.Agent;
import io.fstream.simulate.actor.agent.HFTAgent;
import io.fstream.simulate.actor.agent.InstitutionalAgent;
import io.fstream.simulate.actor.agent.RetailAgent;
import io.fstream.simulate.config.SimulateProperties;

import java.util.List;
import java.util.function.IntFunction;

import lombok.val;
import lombok.extern.slf4j.Slf4j;
import akka.actor.ActorRef;
import akka.actor.Props;

import com.google.common.collect.ImmutableList;

/**
 * Represents a collection of agents.
 */
@Slf4j
public class Broker extends BaseActor {

  public Broker(SimulateProperties properties) {
    super(properties);
  }

  @Override
  public void onReceive(Object message) throws Exception {
    // No-op
  }

  @Override
  public void preStart() throws Exception {
    createAgents();
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

  private List<ActorRef> createInstitutionalAgents() {
    log.info("Creating {} institutional agents...", properties.getInstitutional().getNumAgents());
    return createActors(properties.getInstitutional().getNumAgents(), this::createInstitutionalAgent);
  }

  private List<ActorRef> createHftAgents() {
    log.info("Creating {} hft agents...", properties.getHft().getNumAgents());
    return createActors(properties.getHft().getNumAgents(), this::createHftAgent);
  }

  private static List<ActorRef> createActors(int count, IntFunction<ActorRef> factory) {
    return range(0, count).mapToObj(factory).collect(toList());
  }

  private ActorRef createRetailAgent(int i) {
    return createAgent(RetailAgent.class, "ret" + i);
  }

  private ActorRef createInstitutionalAgent(int i) {
    return createAgent(InstitutionalAgent.class, "inst" + i);
  }

  private ActorRef createHftAgent(int i) {
    return createAgent(HFTAgent.class, "hft" + i);
  }

  private ActorRef createAgent(Class<? extends Agent> agentClass, String name) {
    val props = Props.create(agentClass, properties, name);
    return context().actorOf(props, name);
  }

}

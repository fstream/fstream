package io.fstream.simulate.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import akka.actor.ActorSystem;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

/**
 * Akka configuration.
 */
@Configuration
@Profile("akka")
public class AkkaConfig {

  @Autowired
  SimulateProperties properties;

  @Bean
  public ActorSystem actorSystem() {
    return ActorSystem.create("fstream-simulate", akkaConfiguration());
  }

  @Bean
  public Config akkaConfiguration() {
    return ConfigFactory.load().withValue("akka.scheduler.tick-duration",
        ConfigValueFactory.fromAnyRef(properties.getTickDuration()));
  }

}

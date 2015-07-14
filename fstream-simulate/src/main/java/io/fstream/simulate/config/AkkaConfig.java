package io.fstream.simulate.config;

import lombok.val;

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
    Config config = ConfigFactory.load();

    val profile = properties.isSingleThreaded() ? "single-thread" : "multi-thread";
    config = config.getConfig(profile).withFallback(config);

    val tickDuration = ConfigValueFactory.fromAnyRef(properties.getTickDuration());
    return config.withValue("akka.scheduler.tick-duration", tickDuration);
  }
}

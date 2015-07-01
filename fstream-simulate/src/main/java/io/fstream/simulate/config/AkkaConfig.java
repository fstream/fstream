package io.fstream.simulate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import akka.actor.ActorSystem;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Akka configuration.
 * <p>
 * Currently "toq" is the only profile that makes use of Akka.
 */
@Configuration
@Profile("toq")
public class AkkaConfig {

  @Bean
  public ActorSystem actorSystem() {
    return ActorSystem.create("fstream-simulate", akkaConfiguration());
  }

  @Bean
  public Config akkaConfiguration() {
    return ConfigFactory.load();
  }

}

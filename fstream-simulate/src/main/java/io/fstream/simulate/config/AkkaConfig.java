package io.fstream.simulate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import akka.actor.ActorSystem;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

@Configuration
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

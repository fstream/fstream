package io.fstream.simulate.config;

import java.util.List;

import lombok.val;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

  /**
   * Configuration.
   */
  @Value("#{environment.getActiveProfiles()}")
  List<String> activeProfiles;
  @Autowired
  SimulateProperties properties;

  @Bean
  public ActorSystem actorSystem() {
    return ActorSystem.create("fstream-simulate", akkaConfiguration());
  }

  @Bean
  public Config akkaConfiguration() {
    val baseConfig = ConfigFactory.load();

    val threadProfilePath = "profile." + (properties.isSingleThreaded() ? "single-thread" : "multi-thread");
    Config profileConfig = baseConfig.getConfig(threadProfilePath);

    for (val profile : activeProfiles) {
      val profilePath = "profile." + profile;
      if (baseConfig.hasPath(profilePath)) {
        profileConfig = profileConfig.withFallback(baseConfig.getConfig(profilePath));
      }
    }

    val tickDuration = ConfigValueFactory.fromAnyRef(properties.getTickDuration());
    return profileConfig.withFallback(baseConfig).withValue("akka.scheduler.tick-duration", tickDuration);
  }

}

package io.fstream.simulate.config;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Simulation properties.
 */
@Data
@Component
@ConfigurationProperties("simulate")
public class SimulateProperties {

  /**
   * Reference.
   */
  private List<String> brokers = newArrayList();
  private List<String> instruments = newArrayList();

  /**
   * Settings.
   */
  private int msgResponseTimeout;
  private float minQuoteSize;
  private int nonPremiumQuoteDelay;

  /**
   * Agents.
   */
  private AgentProperties institutional = new AgentProperties();
  private AgentProperties retail = new AgentProperties();
  private AgentProperties hft = new AgentProperties();

  @Data
  public static class AgentProperties {

    private float probBestPrice;
    private float probBuy;
    private float probMarket;

    private int maxTradeSize;
    private int maxSleep; // in millis
    private int minSleep; // in millis

    private int numAgents;

    private String quoteSubscriptionLevel;

  }

}

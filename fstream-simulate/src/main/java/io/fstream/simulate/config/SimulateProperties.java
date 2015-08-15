/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

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
   * Flags.
   */
  private boolean validate;
  private boolean singleThreaded;

  /**
   * Reference.
   */
  private List<String> brokers = newArrayList();
  private List<String> instruments = newArrayList();

  /**
   * Settings.
   */
  private int snapshotInterval;
  private String tickDuration;
  private int msgResponseTimeout;
  private float tickSize;
  private int nonPremiumQuoteDelay;
  private float minPrice;
  private float maxPrice;
  private int riskDistance;

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
    private float probCancel;

    private int maxTradeSize;
    private int maxSleep; // in millis
    private int minSleep; // in millis

    private int numAgents;

    private String quoteSubscriptionLevel;

  }

}

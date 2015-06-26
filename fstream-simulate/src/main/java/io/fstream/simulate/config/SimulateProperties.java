package io.fstream.simulate.config;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("simulate")
public class SimulateProperties {

  private List<String> instruments = newArrayList();

  private int msgResponseTimeout;
  private float minTickSize;
  private int nonPremiumQuoteDelay;
  private List<String> brokers = newArrayList();

  private InstitutionalProperties institutional = new InstitutionalProperties();
  private RetailProperties retail = new RetailProperties();
  private HFTProperties hft = new HFTProperties();

  @Data
  public static class InstitutionalProperties {

    private float probMarket;
    private float probBuy;
    private int maxTradeSize;
    private float probBestPrice;
    private int maxSleep; // in millis
    private int minSleep; // in millis
    private int numAgents;
    private String quoteSubscriptionLevel;

  }

  @Data
  public static class RetailProperties {

    private float probMarket;
    private float probBuy;
    private int maxTradeSize;
    private float probBestPrice;
    private int maxSleep; // in millis
    private int minSleep; // in millis
    private int numAgents;
    private String quoteSubscriptionLevel;

  }

  @Data
  public static class HFTProperties {

    private float probMarket;
    private float probBuy;
    private int maxTradeSize;
    private float probBestPrice;
    private int maxSleep; // in millis
    private int minSleep; // in millis
    private int numAgents;
    private String quoteSubscriptionLevel;

  }

}

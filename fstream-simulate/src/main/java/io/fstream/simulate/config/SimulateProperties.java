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

  private int seconds;

  private List<String> instruments = newArrayList();

  private int msgResponseTimeout;

  private InstitutionalProperties instProp = new InstitutionalProperties();

  private RetailProperties retProp = new RetailProperties();

  @Data
  public static class InstitutionalProperties {

    private float probMarket;
    private float probBuy;
    private int maxTradeSize;
    private float probBestPrice;
    private int maxsleep; // in millis
    private int minsleep; // in millis

  }

  @Data
  public static class RetailProperties {

    private float probMarket;
    private float probBuy;
    private int maxTradeSize;
    private float probBestPrice;
    private int maxsleep; // in millis
    private int minsleep; // in millis

  }

}

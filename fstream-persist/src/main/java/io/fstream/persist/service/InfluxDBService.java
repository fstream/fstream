/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
// @formatter:off

package io.fstream.persist.service;

import static io.fstream.core.model.event.EventType.ALERT;
import static io.fstream.core.model.event.EventType.METRIC;
import static io.fstream.core.model.event.EventType.ORDER;
import static io.fstream.core.model.event.EventType.QUOTE;
import static io.fstream.core.model.event.EventType.TRADE;
import static java.util.function.Predicate.isEqual;
import io.fstream.core.model.event.AlertEvent;
import io.fstream.core.model.event.Event;
import io.fstream.core.model.event.MetricEvent;
import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Quote;
import io.fstream.core.model.event.Trade;
import io.fstream.core.util.Codec;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("influxdb")
public class InfluxDBService implements PersistenceService {

  /**
   * Constants.
   */
  private static final TimeUnit PRECISION = TimeUnit.MILLISECONDS;

  /**
   * Configuration.
   */
  @Value("${influxdb.database}")
  private String databaseName;

  /**
   * Dependencies
   */
  @Autowired
  private InfluxDB influxDb;

  @PostConstruct
  public void initialize() {
    if (!isDBExists()) {
      // Doesn't exist
      log.info("Initializing database '{}'...", databaseName);
      influxDb.createDatabase(databaseName);

      // Register "downsampling continuous query"
      continuousQuery("quotes_1m", "SELECT MEAN(ask) AS ask, MEAN(bid) AS bid INTO quotes_1m FROM quotes GROUP BY time(1m), symbol");
      continuousQuery("quotes_1h", "SELECT MEAN(ask) AS ask, MEAN(bid) AS bid INTO quotes_1h FROM quotes GROUP BY time(1h), symbol");
    }
  }

  @Override
  public void persist(Event event) {
    val point = createPoint(event);
    try {

    influxDb.write(databaseName, "default", point);
    } catch(Exception e) {
      log.error("Error writing point: {}, {}",point, e);
    }
  }

  private Point createPoint(Event event) {
    //
    // Note: All field / tag calls must use non-null values and cannot consume enums.
    //
    
    if (event.getType() == TRADE) {
      val trade = (Trade) event;
      return point("trades", event)
          .tag("symbol", trade.getSymbol())
          .field("amount", trade.getAmount())
          .field("price",trade.getPrice())
          .field("buyUser", trade.getBuyUser())
          .field("sellUser", trade.getSellUser())
          .field("activeBuy", trade.isActiveBuy())
          .build();
    } else if (event.getType() == ORDER) {
      val order = (Order) event;
      val p = point("orders", event)
          .tag("symbol", order.getSymbol())
          .field("amount", order.getAmount())
          .field("price",order.getPrice())
          .field("oid", order.getOid())
          .field("orderType", order.getOrderType().toString())
          .field("side", order.getSide().toString())
          .field("processedTime", order.getProcessedTime().getMillis());

      if (order.getBrokerId() != null) {
        p.field("brokerId", order.getBrokerId());
      }
      if (order.getUserId() != null) {
        p.field("userId", order.getUserId());
      }
      
      return p.build();
    } else if (event.getType() == QUOTE) {
      val quote = (Quote) event;
      return point("quotes", event)
          .tag("symbol", quote.getSymbol())
          .field("ask", quote.getAsk())
          .field("bid",quote.getBid())
          .field("mid", quote.getMid())
          .field("askAmount", quote.getAskAmount())
          .field("bidAmount", quote.getBidAmount())
          .build();
    } else if (event.getType() == METRIC) {
      val metricEvent = (MetricEvent) event;
      return point("metrics", event)
          .field("id", metricEvent.getId())
          .field("data", Codec.encodeText(metricEvent.getData()))          
          .build();
    } else if (event.getType() == ALERT) {
      val alertEvent = (AlertEvent) event;
      return point("alerts", event)
          .field("id", alertEvent.getId())
          .field("data", Codec.encodeText(alertEvent.getData()))          
          .build();
    }

    return null;
  }

  private boolean isDBExists() {
    return influxDb.describeDatabases().stream().anyMatch(isEqual(databaseName));
  }
  
  private void continuousQuery(String name, String text) {
    query("CREATE CONTINUOUS QUERY "  + name + " ON " + databaseName + " BEGIN "  + text + " END");
  }
  
  private QueryResult query(String text) {
    return influxDb.query( new Query(text, databaseName));
  }
  
  private Builder point(String name, Event event) {
    return Point.measurement(name)
        .time(event.getDateTime().getMillis(), PRECISION);
  }

}

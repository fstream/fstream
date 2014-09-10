/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.bolt;

import io.fstream.core.model.definition.Alert;
import io.fstream.core.model.event.AlertEvent;
import io.fstream.core.model.event.Event;
import io.fstream.core.util.Codec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;

import com.espertech.esper.client.EPAdministrator;
import com.fasterxml.jackson.core.type.TypeReference;

@Slf4j
public class AlertBolt extends EsperBolt {

  /**
   * Configuration keys.
   */
  public static final String ALERTS_CONFIG_KEY = Alert.class.getName();

  @Override
  protected void createStatements(Map<?, ?> conf, EPAdministrator admin) {
    val alerts = getAlerts(conf);
    for (val alert : alerts) {
      log.info("Registering alert: {}", alert.getName());
      val statement = admin.createEPL(alert.getStatement(), alert);

      statement.addListener(this);
    }
  }

  @Override
  protected Event createEvent(int id, Object data) {
    return new AlertEvent(new DateTime(), id, data);
  }

  @SneakyThrows
  private static List<Alert> getAlerts(Map<?, ?> conf) {
    val value = (String) conf.get(ALERTS_CONFIG_KEY);

    return Codec.decodeText(value, new TypeReference<ArrayList<Alert>>() {});
  }

}
/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.core.model.event;

import static io.fstream.core.model.event.EventType.TICK;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.joda.time.DateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TickEvent implements Event {

  DateTime dateTime;
  final EventType type = TICK;

  String symbol;
  float ask;
  float bid;
  float mid;

  public TickEvent(DateTime dt, String symbol, float ask, float bid) {
    this.dateTime = dt;
    this.symbol = symbol;
    this.ask = ask;
    this.bid = bid;
    this.mid = (ask + bid) / 2.0f;
  }

}

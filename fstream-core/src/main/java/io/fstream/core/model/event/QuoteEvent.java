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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import org.joda.time.DateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TickEvent extends AbstractEvent {

  private String symbol;
  private float ask;
  private float bid;
  private float mid;

  public TickEvent(@NonNull DateTime dateTime, @NonNull String symbol, float ask, float bid) {
    super(dateTime);
    this.symbol = symbol;
    this.ask = ask;
    this.bid = bid;
    this.mid = (ask + bid) / 2.0f;
  }

  @Override
  public EventType getType() {
    return TICK;
  }

}

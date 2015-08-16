/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.core.model.event;

import static io.fstream.core.model.event.EventType.ALERT;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

import org.joda.time.DateTime;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AlertEvent extends AbstractDerivedEvent {

  private String symbol;

  public AlertEvent(@NonNull DateTime dateTime, int id, String symbol, @NonNull Object data) {
    super(dateTime, id, data);
    this.symbol = symbol;
  }

  public AlertEvent(@NonNull DateTime dateTime, int id, @NonNull Object data) {
    this(dateTime, id, null, data);
  }

  @Override
  public EventType getType() {
    return ALERT;
  }

}

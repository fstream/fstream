/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.core.model.event;

import static io.fstream.core.model.event.EventType.SNAPSHOT;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Snapshot extends AbstractEvent {

  private String symbol;
  private List<Order> orders;
  private Map<Float, Integer> priceLevels;

  @Override
  public EventType getType() {
    return SNAPSHOT;
  }

}

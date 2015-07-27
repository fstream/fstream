/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.core.model.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.joda.time.DateTime;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractDerivedEvent extends AbstractEvent {

  private int id;
  private Object data;

  public AbstractDerivedEvent(DateTime dateTime, int id, Object data) {
    super(dateTime);
    this.id = id;
    this.data = data;
  }

}

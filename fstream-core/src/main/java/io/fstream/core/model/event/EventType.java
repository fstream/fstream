/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.core.model.event;

/**
 * Classification of {@link Event}s.
 */
public enum EventType {

  ALERT,
  TICK,
  TRADE,
  QUOTE,
  ORDER,
  METRIC;

}

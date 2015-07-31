/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.core.model.definition;

import static lombok.AccessLevel.PRIVATE;
import lombok.NoArgsConstructor;

/**
 * System metrics.
 */
@NoArgsConstructor(access = PRIVATE)
public final class Metrics {

  /**
   * Book metrics.
   */
  public static final int TOP_USER_VALUE_ID = 10;
  public static final int TOP_USER_TRADES_ID = 11;
  public static final int TOP_USER_ORDERS_ID = 12;
  public static final int TOP_USER_OT_RATIO_ID = 13;

}

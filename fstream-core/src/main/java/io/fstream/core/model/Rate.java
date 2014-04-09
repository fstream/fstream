/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.core.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class Rate {

  String symbol;
  BigDecimal bid;
  BigDecimal ask;

}

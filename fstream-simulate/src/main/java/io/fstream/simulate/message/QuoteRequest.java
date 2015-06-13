/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.message;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
@Getter
@Setter
public class QuoteRequest {

  String symbol;

  public QuoteRequest(String symbol) {
    this.symbol = symbol;
  }

}

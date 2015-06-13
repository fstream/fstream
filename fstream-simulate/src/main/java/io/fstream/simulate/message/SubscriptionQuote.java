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
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 */
@Getter
@Setter
@NoArgsConstructor
/**
 * Message for subscribing to quotes. Success flag is set to true on success by exchange a sent back to sender.
 */
public class SubscriptionQuote {

  private String level;
  private boolean success;

  public SubscriptionQuote(String level) {
    this.level = level;
    this.success = false;
  }

}

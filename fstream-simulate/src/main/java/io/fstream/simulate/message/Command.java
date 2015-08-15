/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.simulate.message;

/**
 * Simple actor command messages that carry no data.
 */
public enum Command {

  AGENT_EXECUTE_ACTION,
  PRINT_BOOK,
  SEND_BOOK_SNAPSHOT,

  PRINT_SUMMARY,

  SUBSCRIBE_QUOTES,
  SUBSCRIBE_QUOTES_ORDERS,
  SUBSCRIBE_QUOTES_PREMIUM;

}

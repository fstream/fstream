package io.fstream.simulate.message;

/**
 * Simple actor command messages that carry no data.
 */
public enum Command {

  AGENT_EXECUTE_ACTION,
  PRINT_ORDER_BOOK,
  PRINT_SUMMARY,
  SUBSCRIBE_QUOTES,
  SUBSCRIBE_QUOTES_ORDERS,
  SUBSCRIBE_QUOTES_PREMIUM;

}

package io.fstream.simulate.message;

/**
 * Simple actor messages that carry no data.
 */
public enum Command {

  AGENT_EXECUTE_ACTION,
  PRINT_ORDER_BOOK,
  REGISTER_TRADE,
  PRINT_SUMMARY,
  SUBSCRIBE_QUOTES,
  SUBSCRIBE_QUOTES_ORDERS,
  SUBSCRIBE_QUOTES_PREMIUM;

}

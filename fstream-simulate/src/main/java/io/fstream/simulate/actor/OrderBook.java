package io.fstream.simulate.actor;

import static com.google.common.base.Preconditions.checkState;
import static io.fstream.core.model.event.Order.OrderSide.ASK;
import static io.fstream.core.model.event.Order.OrderSide.BID;
import static io.fstream.core.model.event.Order.OrderType.LIMIT_ADD;
import static io.fstream.core.model.event.Order.OrderType.LIMIT_AMEND;
import static io.fstream.core.model.event.Order.OrderType.LIMIT_CANCEL;
import static io.fstream.core.model.event.Order.OrderType.MARKET_ORDER;
import static io.fstream.simulate.util.OrderBookFormatter.formatOrderBook;
import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Quote;
import io.fstream.core.model.event.Trade;
import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.message.Command;
import io.fstream.simulate.util.BookSide;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

/**
 * A price,time ordered implementation of a central limit order book. The principle data structure is a List of
 * {@code Order}s. By definition only limit orders can live in the book. Market orders are accepted and trigger a trade
 * immediately if liquidity is available.
 */
@Slf4j
@Getter
@ToString(of = "symbol")
public class OrderBook extends BaseActor {

  /**
   * Configuration.
   */
  @NonNull
  private final String symbol;

  /**
   * State.
   */
  private final BookSide asks = new BookSide(ASK);
  private final BookSide bids = new BookSide(BID);

  /**
   * Aggregation caches.
   */
  private float bestAsk = asks.getBestPrice();
  private float bestBid = bids.getBestPrice();

  private int orderCount = 0;
  private int tradeCount = 0;

  public OrderBook(SimulateProperties properties, String symbol) {
    super(properties);
    this.symbol = symbol;
  }

  public float getSpread() {
    return bestAsk - bestBid;
  }

  public void printBook() {
    log.info("\n{}", formatOrderBook(this));
  }

  public void printSummary() {
    log.info("{} orders processed={}, trades processed={}, bidDepth={}, askDepth={} bestAsk={} bestBid={} spread={}",
        symbol, orderCount, tradeCount, bids.getDepth(), asks.getDepth(), bestAsk, bestBid, getSpread());
  }

  public void printStatus() {
    log.info("[{}] trade count = {}, ask count = {}, bid count = {}, ask depth = {}, bid depth = {}",
        symbol, tradeCount, asks.calculateOrderCount(), bids.calculateOrderCount(), asks.getDepth(), bids.getDepth());
  }

  @Override
  public void onReceive(Object message) throws Exception {
    log.debug("{} message received {}", this, message);

    if (message instanceof Order) {
      onReceiveOrder((Order) message);
    } else if (message instanceof Command) {
      onReceiveCommand((Command) message);
    } else {
      unhandled(message);
    }
  }

  private void onReceiveOrder(Order order) {
    checkState(order.getSymbol() == symbol, "Received unexpected symbol '%s' for book '%s'", order.getSymbol(), this);

    // Book keeping
    orderCount += 1;

    if (orderCount % 100_000 == 0) {
      printStatus();
    }

    log.debug("Processing {} order: {}", order.getOrderType(), order);
    order.setProcessedTime(getSimulationTime());

    if (order.getOrderType() == MARKET_ORDER) {
      // TODO: Explain the need for this. Perhaps this should go into executeOrder.
      val price = order.getSide() == ASK ? Float.MIN_VALUE : Float.MAX_VALUE;
      order.setPrice(price);

      // TODO: Explain what happens if the order cannot be completely filled. Should it be rejected?
      executeOrder(order);
    } else if (order.getOrderType() == LIMIT_ADD) {
      int unfilledSize = order.getAmount();
      if (isExecutable(order)) {
        // If limit price crosses spread, treat as market order
        unfilledSize = executeOrder(order);

        order.setAmount(unfilledSize);
      }

      // Any unfilled amount added to order book
      if (unfilledSize > 0) {
        insertOrder(order);
      }
    } else if (order.getOrderType() == LIMIT_AMEND) {
      // TODO: Add support for amend?
    } else if (order.getOrderType() == LIMIT_CANCEL) {
      cancelOrder(order);
    } else {
      checkState(false);
    }

    if (properties.isDebug() && !assertBookDepth()) {
      System.exit(1);
    }
  }

  private void onReceiveCommand(Command command) {
    if (command == Command.PRINT_ORDER_BOOK) {
      printBook();
    } else if (command == Command.PRINT_SUMMARY) {
      printSummary();
    }
  }

  /**
   * Accepts {@code Order} and executes it against available depth.
   * 
   * @return Unfilled amount.
   */
  // TODO: Currently market orders are mimicked via {@code Order}s where trigger price is best ask/bid. Need to add
  // marketable order implementation.
  private int executeOrder(Order order) {
    // Match against passive side
    val bookSide = getOppositeBookSide(order);
    int unfilledSize = order.getAmount();

    // Iterate in (price, time) order
    val orderIterator = bookSide.iterator();
    while (orderIterator.hasNext()) {
      val passiveOrder = orderIterator.next();

      if (isBreakingPriceCrossed(order, passiveOrder)) {
        // Limit price exists, respect bounds
        break;
      }

      // Account for current passive order
      unfilledSize -= passiveOrder.getAmount();

      if (unfilledSize == 0) {
        // Filled
        executeTrade(order, passiveOrder, order.getAmount());

        // Remove the passive order
        orderIterator.remove();

        // Finished filling
        break;
      } else if (unfilledSize < 0) {
        // Remaining unfilled was smaller than the current passive order
        passiveOrder.setAmount(Math.abs(unfilledSize));

        executeTrade(order, passiveOrder, order.getAmount());

        // TODO: Make this automatic
        bookSide.removeDepth(order.getAmount());

        // Finished filling
        break;
      } else if (unfilledSize > 0) {
        // Remaining unfilled is larger than the current passive order.
        order.setAmount(order.getAmount() - passiveOrder.getAmount());

        executeTrade(order, passiveOrder, passiveOrder.getAmount());

        // Remove the passive order
        orderIterator.remove();

        // Continue filling
        continue;
      }
    }

    updateQuote();

    return unfilledSize;
  }

  /**
   * Registers a trade
   */
  private void executeTrade(Order active, Order passive, int executedSize) {
    // Book keeping
    tradeCount += 1;

    val trade = new Trade(getSimulationTime(), active, passive, executedSize);

    // Publish
    exchange().tell(trade, self());
    publisher().tell(trade, self());

    val latency = calculateLatency(active.getDateTime(), trade.getDateTime());
    val delayed = latency > 5;
    if (delayed) {
      log.warn("Order took more than 5 seconds to be processed {}", active);
    }
  }

  /**
   * Updates best ask and bid.
   */
  private void updateQuote() {
    val prevBestAsk = bestAsk;
    bestAsk = asks.getBestPrice();

    val prevBestBid = bestBid;
    bestBid = bids.getBestPrice();

    val invalid = bestAsk <= bestBid;
    if (invalid) {
      log.error("Invalid quote [ask = {}, bid = {}]", bestAsk, bestBid);
      return;
    }

    val changed = bestAsk != prevBestAsk || bestBid != prevBestBid;
    if (changed) {
      val quote = new Quote(getSimulationTime(), symbol, bestAsk, bestBid,
          asks.calculatePriceDepth(bestAsk),
          bids.calculatePriceDepth(bestBid));

      // Publish
      exchange().tell(quote, self());
      publisher().tell(quote, self());
    }
  }

  /**
   * Inserts limit order in the book side data structure.
   */
  private void insertOrder(Order order) {
    getBookSide(order).addOrder(order);

    // Set best price and depth attributes
    if (order.getSide() == ASK && (asks.getDepth() == 0 || order.getPrice() < bestAsk)) {
      bestAsk = order.getPrice();
    } else if (order.getSide() == BID && (bids.getDepth() == 0 || order.getPrice() > bestBid)) {
      bestBid = order.getPrice();
    }

    val latency = calculateLatency(order.getProcessedTime(), order.getDateTime());
    val delayed = latency > 5;
    if (delayed) {
      log.debug("Order took more than 5 seconds to be processed: {}", order);
    }

    // publish to tape
    publisher().tell(order, self());
  }

  /**
   * Cancels and removes an order from this order book.
   */
  private boolean cancelOrder(Order order) {
    val removed = getBookSide(order).removeOrder(order);
    if (!removed) {
      return false;
    }

    log.debug("Cancelled order {}", order);
    publisher().tell(order, self());

    return true;
  }

  private boolean isExecutable(Order order) {
    val depthAvailable = getOppositeBookSide(order).getDepth() > 0;

    return depthAvailable && isSpreadCrossed(order);
  }

  private boolean isBreakingPriceCrossed(Order order, Order passiveOrder) {
    return order.getSide() == ASK && order.getPrice() > passiveOrder.getPrice() ||
        order.getSide() == BID && order.getPrice() < passiveOrder.getPrice();
  }

  /**
   * Determines if a limit order crosses the spread
   * <p>
   * Happens when buy is better priced than best ask or sell is better priced than best bid.
   */
  private boolean isSpreadCrossed(Order order) {
    return order.getSide() == ASK && order.getPrice() <= bestBid ||
        order.getSide() == BID && order.getPrice() >= bestAsk;
  }

  /**
   * Checks the validity of the book by inspecting actual depth in the book and comparing it to maintained bid depth /
   * ask depth variables.
   */
  private boolean assertBookDepth() {
    return asks.isDepthValid() && bids.isDepthValid();
  }

  private BookSide getBookSide(Order order) {
    return order.getSide() == ASK ? asks : bids;
  }

  private BookSide getOppositeBookSide(Order order) {
    return order.getSide() == ASK ? bids : asks;
  }

  private int calculateLatency(DateTime endTime, DateTime startTime) {
    return Seconds.secondsBetween(endTime, startTime).getSeconds();
  }

}

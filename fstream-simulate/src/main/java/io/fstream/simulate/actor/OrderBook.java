package io.fstream.simulate.actor;

import static com.google.common.base.Preconditions.checkState;
import static io.fstream.core.model.event.Order.OrderSide.ASK;
import static io.fstream.core.model.event.Order.OrderSide.BID;
import static io.fstream.core.model.event.Order.OrderType.MARKET_ORDER;
import static io.fstream.simulate.util.OrderBookFormatter.formatOrderBook;
import static java.util.Collections.reverseOrder;
import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Order.OrderSide;
import io.fstream.core.model.event.Order.OrderType;
import io.fstream.core.model.event.Quote;
import io.fstream.core.model.event.Trade;
import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.message.Command;
import io.fstream.simulate.util.LimitOrderTimeComparator;

import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;

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
  private final NavigableMap<Float, NavigableSet<Order>> asks = new TreeMap<>(); // Ascending price
  private final NavigableMap<Float, NavigableSet<Order>> bids = new TreeMap<>(reverseOrder()); // Descending price

  /**
   * Aggregation caches.
   */
  private float bestAsk = calculateBestAsk();
  private float bestBid = calculateBestBid();

  private long askDepth;
  private long bidDepth;

  private int orderCount = 0;
  private int tradeCount = 0;

  public OrderBook(SimulateProperties properties, String symbol) {
    super(properties);
    this.symbol = symbol;
  }

  public void printBook() {
    log.info("\n{}", formatOrderBook(this));
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
      log.info("[{}] trade count = {}, ask count = {}, bid count = {}, ask depth = {}, bid depth = {}",
          symbol, tradeCount, calculateAskCount(), calculateBidCount(), askDepth, bidDepth);
    }

    log.debug("Processing {} order: {}", order.getOrderType(), order);
    order.setProcessedTime(getSimulationTime());

    if (order.getOrderType() == MARKET_ORDER) {
      // TODO: Explain the need for this. Perhaps this should go into processMarketOrder.
      val price = order.getSide() == ASK ? Float.MIN_VALUE : Float.MAX_VALUE;
      order.setPrice(price);

      // TODO: Explain what happens if the order cannot be completely filled. Should it be rejected?
      processMarketOrder(order);
    } else {
      processLimitOrder(order);
    }

    if (properties.isDebug() && !assertBookDepth()) {
      System.exit(1);
    }
  }

  private void onReceiveCommand(Command command) {
    if (command == Command.PRINT_ORDER_BOOK) {
      printBook();

      // TODO: Explain what does sending "true" achieve
      sender().tell(true, self());
    } else if (command == Command.PRINT_SUMMARY) {
      val spread = calculateSpread();
      log.info("{} orders processed={}, trades processed={}, bidDepth={}, askDepth={} bestAsk={} bestBid={} spread={}",
          symbol, orderCount, tradeCount, bidDepth, askDepth, bestAsk, bestBid, spread);
    }
  }

  /**
   * Accepts {@code Order} and executes it against available depth.
   * 
   * @return Unfilled amount.
   */
  // TODO: Currently market orders are mimicked via {@code Order}s where trigger price is best ask/bid. Need to add
  // marketable order implementation.
  private int processMarketOrder(Order order) {
    // Match against opposite side
    val bookSide = order.getSide() == ASK ? bids : asks;
    if (bookSide.isEmpty()) {
      log.debug("No depth. Order not filled {}", order);
      return order.getAmount();
    }

    int executedSize = 0;
    int totalExecutedSize = 0;
    int unfilledSize = order.getAmount();

    // Iterate in price order
    val priceLevelIterator = bookSide.values().iterator();

    execution: while (priceLevelIterator.hasNext()) {
      val priceLevel = priceLevelIterator.next();

      // Iterate in time order
      val orderIterator = priceLevel.iterator();
      while (orderIterator.hasNext()) {
        val passiveOrder = orderIterator.next();

        val priceCrossed =
            order.getSide() == ASK && order.getPrice() > passiveOrder.getPrice() ||
                order.getSide() == BID && order.getPrice() < passiveOrder.getPrice();

        if (priceCrossed) {
          // Limit price exists, respect bounds
          log.debug("Breaking price crossed on active {} MO for {} order price={} passive order={}",
              order.getSide(), symbol, order.getPrice(), passiveOrder.getPrice());

          break execution;
        }

        // Account for current passive order
        unfilledSize -= passiveOrder.getAmount();

        if (unfilledSize == 0) {
          // Filled
          executedSize = order.getAmount();
          executeTrade(order, passiveOrder, executedSize);

          // Remove the passive order
          orderIterator.remove();
          if (priceLevel.isEmpty()) {
            priceLevelIterator.remove();
          }

          totalExecutedSize += executedSize;

          // Finished
          break execution;
        } else if (unfilledSize < 0) {
          // Remaining unfilled was smaller than the current passive order
          passiveOrder.setAmount(Math.abs(unfilledSize));

          executedSize = order.getAmount();
          executeTrade(order, passiveOrder, executedSize);

          totalExecutedSize += executedSize;

          // Finished
          break execution;
        } else if (unfilledSize > 0) {
          // Remaining unfilled is larger than the current passive order.
          order.setAmount(order.getAmount() - passiveOrder.getAmount());

          executedSize = passiveOrder.getAmount();
          executeTrade(order, passiveOrder, executedSize);

          // Remove the passive order
          orderIterator.remove();
          if (priceLevel.isEmpty()) {
            priceLevelIterator.remove();
          }

          totalExecutedSize += executedSize;

          // Continue filling
          continue;
        }
      }
    }

    updateDepth(order.getSide(), totalExecutedSize);
    updateQuote();

    return unfilledSize;
  }

  private void processLimitOrder(Order order) {
    if (order.getOrderType() == OrderType.LIMIT_ADD) {
      log.debug("Order added");
      addLimitOrder(order);
    } else if (order.getOrderType() == OrderType.LIMIT_AMEND) {
      log.debug("Order amended");
      // TODO: Add support?
    } else if (order.getOrderType() == OrderType.LIMIT_CANCEL) {
      log.debug("Cancelling order {}", order);
      cancelOrder(order);
    } else {
      checkState(false);
    }
  }

  /**
   * Updates best ask and bid.
   */
  private void updateQuote() {
    val prevBestAsk = bestAsk;
    val prevBestBid = bestBid;

    bestAsk = calculateBestAsk();
    bestBid = calculateBestBid();

    val changed = this.bestAsk != prevBestAsk || this.bestBid != prevBestBid;
    if (changed) {
      val quote = new Quote(getSimulationTime(), symbol, bestAsk, bestBid,
          calculatePriceLevelDepth(ASK, bestAsk),
          calculatePriceLevelDepth(BID, bestBid));

      val valid = bestAsk > bestBid;
      if (!valid) {
        log.error("Invalid quote {}", quote);
        return;
      }

      // Publish
      exchange().tell(quote, self());
      publisher().tell(quote, self());
    }
  }

  /**
   * Updates bid depth / ask depth based on executed size
   */
  private void updateDepth(OrderSide side, int executedSize) {
    if (side == ASK) {
      bidDepth -= executedSize;
    } else {
      askDepth -= executedSize;
    }
  }

  /**
   * Checks the validity of the book by inspecting actual depth in the book and comparing it to maintained bid depth /
   * ask depth variables
   */
  private boolean assertBookDepth() {
    val bidDepth = calculateBidDepth();
    if (bidDepth != this.bidDepth) {
      log.error("Bid depth does not add up record = {} actual = {}", this.bidDepth, bidDepth);
      return false;
    }

    val askDepth = calculateAskDepth();
    if (askDepth != this.askDepth) {
      log.error("Ask depth does not add up record = {} actual = {}", this.askDepth, askDepth);
      return false;
    }

    return true;
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
   * Adds the supplied limit order to the order book.
   */
  private void addLimitOrder(Order order) {
    val availableDepth = order.getSide() == ASK ? bidDepth : askDepth;

    val executable = isCrossesSpread(order) && availableDepth > 0;
    if (executable) {
      // If limit price crosses spread, treat as market order
      val unfilledSize = processMarketOrder(order);

      order.setAmount(unfilledSize);

      // Any unfilled amount added to order book
      if (unfilledSize > 0) {
        insertOrder(order);
      }
    } else {
      // Not crossing spread or no depth available. So add to limit book
      insertOrder(order);
    }
  }

  /**
   * Inserts limit order in the book side data structure.
   */
  private void insertOrder(Order order) {
    val priceLevel = resolvePriceLevel(order);
    priceLevel.add(order);

    // Set best price and depth attributes
    if (order.getSide() == BID) {
      if (bidDepth == 0 || order.getPrice() > bestBid) {
        bestBid = order.getPrice();
      }

      bidDepth += order.getAmount();
    } else {
      if (askDepth == 0 || order.getPrice() < bestAsk) {
        bestAsk = order.getPrice();
      }

      askDepth += order.getAmount();
    }

    // TODO: Is this needed? It is done in onReceiveOrder
    order.setProcessedTime(getSimulationTime());

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
    val priceLevel = getPriceLevel(order);

    val empty = priceLevel == null;
    if (empty) {
      return false;
    }

    val missing = !priceLevel.remove(order);
    if (missing) {
      return false;
    }

    if (priceLevel.isEmpty()) {
      removePriceLevel(order);
    }

    // Reduce depth due to cancellation
    if (order.getSide() == ASK) {
      askDepth -= order.getAmount();
    } else {
      bidDepth -= order.getAmount();
    }

    log.debug("Cancelled order {}", order);
    publisher().tell(order, self());

    return true;
  }

  /**
   * Determines if a limit order crosses the spread
   * <p>
   * Happens when buy is better priced than best ask or sell is better priced than best bid.
   */
  private boolean isCrossesSpread(Order order) {
    if (order.getSide() == ASK && order.getPrice() <= bestBid) {
      return true;
    } else if (order.getSide() == BID && order.getPrice() >= bestAsk) {
      return true;
    } else {
      return false;
    }
  }

  private NavigableSet<Order> resolvePriceLevel(Order order) {
    val priceLevel = getPriceLevel(order);
    val exists = priceLevel != null;
    if (exists) {
      return priceLevel;
    }

    val newPriceLevel = createPriceLevel();
    getBookSide(order).put(order.getPrice(), newPriceLevel);

    return newPriceLevel;
  }

  private TreeSet<Order> createPriceLevel() {
    return new TreeSet<Order>(LimitOrderTimeComparator.INSTANCE);
  }

  private NavigableSet<Order> getPriceLevel(Order order) {
    return getPriceLevel(order.getSide(), order.getPrice());
  }

  private NavigableSet<Order> getPriceLevel(OrderSide side, float price) {
    return getBookSide(side).get(price);
  }

  private NavigableSet<Order> removePriceLevel(Order order) {
    return removePriceLevel(order.getSide(), order.getPrice());
  }

  private NavigableSet<Order> removePriceLevel(OrderSide side, float price) {
    return getBookSide(side).remove(price);
  }

  private NavigableMap<Float, NavigableSet<Order>> getBookSide(Order order) {
    return getBookSide(order.getSide());
  }

  private NavigableMap<Float, NavigableSet<Order>> getBookSide(OrderSide side) {
    return side == OrderSide.ASK ? asks : bids;
  }

  private float calculateSpread() {
    return bestAsk - bestBid;
  }

  private float calculateBestBid() {
    return bids.isEmpty() ? Float.MIN_VALUE : bids.firstKey();
  }

  private float calculateBestAsk() {
    return asks.isEmpty() ? Float.MAX_VALUE : asks.firstKey();
  }

  private int calculatePriceLevelDepth(OrderSide side, float price) {
    val priceLevel = getPriceLevel(side, price);
    if (priceLevel == null) {
      return 0;
    }

    int depth = 0;
    for (val order : priceLevel) {
      depth += order.getAmount();
    }

    return depth;
  }

  private int calculateAskDepth() {
    return calculateBookSideDepth(asks);
  }

  private int calculateBidDepth() {
    return calculateBookSideDepth(bids);
  }

  private int calculateBookSideDepth(NavigableMap<Float, NavigableSet<Order>> bookSide) {
    int depth = 0;
    for (val priceLevel : bookSide.values()) {
      for (val order : priceLevel) {
        depth += order.getAmount();
      }
    }

    return depth;
  }

  private int calculateAskCount() {
    return calculateBookSideCount(asks);
  }

  private int calculateBidCount() {
    return calculateBookSideCount(bids);
  }

  private int calculateBookSideCount(NavigableMap<Float, NavigableSet<Order>> bookSide) {
    int count = 0;
    for (val priceLevel : bookSide.values()) {
      count += priceLevel.size();
    }

    return count;
  }

  private int calculateLatency(DateTime endTime, DateTime startTime) {
    return Seconds.secondsBetween(endTime, startTime).getSeconds();
  }

}

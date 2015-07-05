package io.fstream.simulate.actor;

import static com.google.common.base.Preconditions.checkState;
import static io.fstream.core.model.event.Order.OrderSide.ASK;
import static io.fstream.core.model.event.Order.OrderSide.BID;
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
public class OrderBook extends BaseActor {

  /**
   * Configuration.
   */
  @NonNull
  private final String symbol;

  /**
   * State.
   */
  private final NavigableMap<Float, NavigableSet<Order>> asks = new TreeMap<>();
  private final NavigableMap<Float, NavigableSet<Order>> bids = new TreeMap<>(reverseOrder()); // Non-natural

  /**
   * Aggregates.
   */
  private float bestAsk = Float.MAX_VALUE;
  private float bestBid = Float.MIN_VALUE;

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
    log.debug("Order book message received {}", message);

    if (message instanceof Order) {
      onReceiveOrder((Order) message);
    } else if (message instanceof Command) {
      onReceiveCommand((Command) message);
    } else {
      unhandled(message);
    }
  }

  private void onReceiveOrder(Order order) {
    checkState(order.getSymbol() == symbol);

    // Book keeping
    orderCount += 1;

    if (orderCount % 100_000 == 0) {
      log.info("[{}] ask count = {}, bid count = {}, ask depth = {}, bid depth = {}",
          symbol, calculateAskCount(), calculateBidCount(), askDepth, bidDepth);
    }

    log.debug("Processing {} order: {}", order.getOrderType(), order);
    order.setProcessedTime(getSimulationTime());

    if (order.getOrderType() == OrderType.MARKET_ORDER) {
      // TODO: Explain the need for this. Perhaps this should go into processMarketOrder.
      val price = order.getSide() == ASK ? Float.MIN_VALUE : Float.MAX_VALUE;
      order.setPrice(price);

      processMarketOrder(order);
    } else {
      processLimitOrder(order);
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
   * Accepts {@code Order} and executes it against available depth. Returns unfilled amount.
   * <p>
   * TODO: Currently MarketOrders are mimicked via Orders where trigger price is best ask/bid. Need to add marketable
   * order implementation
   */
  private int processMarketOrder(Order order) {
    val bookSide = order.getSide() == OrderSide.ASK ? bids : asks;
    if (bookSide.isEmpty()) {
      log.debug("No depth. Order not filled {}", order);
      return order.getAmount();
    }

    int unfilledSize = order.getAmount();
    int executedSize = 0;
    int totalExecutedSize = 0;

    val bookIterator = bookSide.entrySet().iterator();
    while (bookIterator.hasNext()) {
      val priceLevel = bookIterator.next();

      val orderIterator = priceLevel.getValue().iterator();
      while (orderIterator.hasNext()) {
        val passiveOrder = orderIterator.next();

        if (unfilledSize <= 0) {
          break;
        }

        if (order.getSide() == ASK) {
          // Limit price exists, respect bounds
          if (order.getPrice() > passiveOrder.getPrice()) {
            log.debug("Breaking price crossed on active ASK (SELL) MO for {} order price={} passive order={}",
                this.getSymbol(), order.getPrice(), passiveOrder.getPrice());
            this.updateDepth(order.getSide(), totalExecutedSize);
            this.updateBestPrices();

            return unfilledSize; // price has crossed
          }
        } else {
          if (order.getPrice() < passiveOrder.getPrice()) {
            log.debug("Breaking price crossed on active BID (BUY) MO for {} order price={} passive order={}",
                this.getSymbol(), order.getPrice(), passiveOrder.getPrice());
            this.updateDepth(order.getSide(), totalExecutedSize);
            this.updateBestPrices();

            return unfilledSize; // price has crossed
          }
        }
        unfilledSize -= passiveOrder.getAmount();

        if (unfilledSize == 0) {
          // Nothing else to do.
          executedSize = order.getAmount();
          totalExecutedSize += executedSize;

          executeTrade(order, passiveOrder, executedSize);

          // Remove the passive order (last one returned by iterator)
          orderIterator.remove();
        } else if (unfilledSize < 0) {
          // Incoming was smaller than first order in queue. Repost remainder
          executedSize = order.getAmount();
          totalExecutedSize += executedSize;
          passiveOrder.setAmount(Math.abs(unfilledSize));

          executeTrade(order, passiveOrder, executedSize);
        } else {
          // Incoming larger than the first order in current level. Keep on iterating.
          executedSize = passiveOrder.getAmount();
          totalExecutedSize += executedSize;
          order.setAmount(order.getAmount() - executedSize);

          executeTrade(order, passiveOrder, executedSize);

          // Remove the passive order (last one returned by iterator)
          orderIterator.remove();
        }
      }

      val price = priceLevel.getKey();
      if (bookSide.get(price).isEmpty()) {
        // Removes price level if order queue in it is empty (last one returned by iterator)
        bookIterator.remove();
      }
    }

    updateDepth(order.getSide(), totalExecutedSize);
    updateBestPrices();

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
  private void updateBestPrices() {
    val prevBestAsk = bestAsk;
    val prevBestBid = bestBid;

    bestAsk = calculateBestAsk();
    bestBid = calculateBestBid();

    val changed = this.bestAsk != prevBestAsk || this.bestBid != prevBestBid;
    if (changed) {
      val quote = new Quote(getSimulationTime(), symbol, bestAsk, bestBid,
          calculatePriceLevelDepth(bestAsk, ASK),
          calculatePriceLevelDepth(bestBid, BID));

      if (!isValidQuote(bestAsk, bestBid)) {
        log.error("Invalid quote {}", quote);
        return;
      }

      // Publish
      exchange().tell(quote, self());
      publisher().tell(quote, self());
    }
  }

  private boolean isValidQuote(float ask, float bid) {
    return ask > bid;
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

    if (isSpreadCrossed(order) && availableDepth > 0) {
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

    if (log.isDebugEnabled() && !assertBookDepth()) {
      System.exit(1);
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

    if (order.getSide() == ASK) {
      askDepth -= order.getAmount();
    } else {
      bidDepth -= order.getAmount();
    }

    if (log.isDebugEnabled() && !assertBookDepth()) {
      System.exit(1);
    }

    log.debug("Cancelled order");
    publisher().tell(order, self());

    return true;
  }

  /**
   * Determines if a limit order crosses the spread
   * <p>
   * Happens when buy is better priced than best ask or sell is better priced than best bid.
   */
  private boolean isSpreadCrossed(Order order) {
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

    val newPriceLevel = new TreeSet<Order>(LimitOrderTimeComparator.INSTANCE);
    getBookSide(order).put(order.getPrice(), newPriceLevel);

    return newPriceLevel;
  }

  private NavigableSet<Order> getPriceLevel(Order order) {
    return getPriceLevel(order.getSide(), order.getPrice());
  }

  private NavigableSet<Order> getPriceLevel(OrderSide side, float price) {
    return getBookSide(side).get(price);
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

  private int calculatePriceLevelDepth(float price, OrderSide side) {
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
    int askDepth = 0;
    for (val values : asks.values()) {
      for (val asks : values) {
        askDepth += asks.getAmount();
      }
    }

    return askDepth;
  }

  private int calculateBidDepth() {
    int bidDepth = 0;
    for (val values : bids.values()) {
      for (val bids : values) {
        bidDepth += bids.getAmount();
      }
    }

    return bidDepth;
  }

  private int calculateAskCount() {
    int askCount = 0;
    for (val values : asks.values()) {
      askCount += values.size();
    }

    return askCount;
  }

  private int calculateBidCount() {
    int bidCount = 0;
    for (val values : bids.values()) {
      bidCount += values.size();
    }

    return bidCount;
  }

  private int calculateLatency(DateTime endTime, DateTime startTime) {
    return Seconds.secondsBetween(endTime, startTime).getSeconds();
  }

}

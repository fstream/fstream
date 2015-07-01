package io.fstream.simulate.actor;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.reverseOrder;
import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.message.Command;
import io.fstream.simulate.model.LimitOrder;
import io.fstream.simulate.model.Order;
import io.fstream.simulate.model.Order.OrderSide;
import io.fstream.simulate.model.Order.OrderType;
import io.fstream.simulate.model.Quote;
import io.fstream.simulate.model.Trade;
import io.fstream.simulate.util.LimitOrderTimeComparator;
import io.fstream.simulate.util.OrderBookFormatter;

import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

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
  final String symbol;

  /**
   * State.
   */
  final NavigableMap<Float, NavigableSet<LimitOrder>> bids = new TreeMap<>(reverseOrder()); // Non-natural
  final NavigableMap<Float, NavigableSet<LimitOrder>> asks = new TreeMap<>();

  /**
   * Aggregates.
   */
  float bestBid = Float.MIN_VALUE;
  float bestAsk = Float.MIN_VALUE;

  int bidDepth;
  int askDepth;

  int orderCount = 0;
  int tradeCount = 0;

  public OrderBook(SimulateProperties properties, String symbol) {
    super(properties);
    this.symbol = symbol;
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
    checkState(order.getSymbol() == this.symbol);

    orderCount += 1;
    order.setProcessedTime(getSimulationTime());
    if (order.getType() == OrderType.MO) {
      // Process market order
      log.debug("Processing market order: {}", order);
      val limitOrder = (LimitOrder) order;
      if (limitOrder.getSide() == OrderSide.ASK) {
        limitOrder.setPrice(Float.MIN_VALUE);
      } else {
        limitOrder.setPrice(Float.MAX_VALUE);
      }

      this.processMarketOrder(limitOrder);
    } else {
      // Process limit order
      log.debug("Processing limitorder: {}", order);
      this.processLimitOrder((LimitOrder) order);
    }
  }

  private void onReceiveCommand(Command command) {
    if (command == Command.PRINT_ORDER_BOOK) {
      this.printBook();

      // TODO: What does true do here?
      sender().tell(true, self());
    } else if (command == Command.PRINT_SUMMARY) {
      log.info("{} orders processed={}, trades processed={}, biddepth={}, askdepth={} bestask={} bestbid={} spread={}",
          symbol, orderCount, tradeCount, bidDepth, askDepth, bestAsk, bestBid, bestAsk - bestBid);
    }
  }

  /**
   * Accepts IOrder and executes it against available depth. Returns unfilled amount TODO currently MarketOrders are
   * mimicked via LimitOrders where trigger price is best ask/bid. Need to add marketable order implementation
   */
  private int processMarketOrder(LimitOrder order) {
    NavigableMap<Float, NavigableSet<LimitOrder>> book;
    if (order.getSide() == OrderSide.ASK) {
      if (this.bids.isEmpty()) {
        log.debug("No depth. Order not filled {}", order);
        return order.getAmount();
      }
      book = this.bids;
    } else {
      if (this.asks.isEmpty()) {
        log.debug("No depth. Order not filled {}", order);
        return order.getAmount();
      }
      book = this.asks;
    }

    int unfilledsize = order.getAmount();
    int executedsize = 0;
    int totalexecutedsize = 0;
    val bookiterator = book.entrySet().iterator();
    while (bookiterator.hasNext()) {
      val pricelevel = bookiterator.next();
      val orderiterator = pricelevel.getValue().iterator();
      while (orderiterator.hasNext()) {
        val passiveorder = orderiterator.next();

        if (unfilledsize <= 0) {
          break;
        }
        if (order.getSide() == OrderSide.ASK) { // limit price exists,
          // respect bounds
          if (order.getPrice() > passiveorder.getPrice()) {
            log.debug("Breaking price crossed on active ASK (SELL) MO for {} orderprice={} passiveorder={}",
                this.getSymbol(), order.getPrice(), passiveorder.getPrice());
            this.updateDepth(order.getSide(), totalexecutedsize);
            this.updateBestPrices();
            return unfilledsize; // price has crossed
          }
        } else {
          if (order.getPrice() < passiveorder.getPrice()) {
            log.debug("breaking price crossed on active BID (BUY) MO for {} orderprice={} passiveorder={}",
                this.getSymbol(), order.getPrice(), passiveorder.getPrice());
            this.updateDepth(order.getSide(), totalexecutedsize);
            this.updateBestPrices();
            return unfilledsize; // price has crossed
          }
        }
        unfilledsize = unfilledsize - passiveorder.getAmount();

        if (unfilledsize == 0) {
          // nothing else to do.
          executedsize = order.getAmount();
          totalexecutedsize = totalexecutedsize + executedsize;
          registerTrade(order, passiveorder, executedsize);
          orderiterator.remove(); // remove the passive order (last
          // one returned by iterator)

        } else if (unfilledsize < 0) {
          // incoming was smaller than first order in queue. repost
          // remainder
          executedsize = order.getAmount();
          totalexecutedsize = totalexecutedsize + executedsize;
          passiveorder.setAmount(Math.abs(unfilledsize));
          registerTrade(order, passiveorder, executedsize);

        } else {
          // incoming larger than the first order in current level.
          // keep
          // on iterating.
          executedsize = passiveorder.getAmount();
          order.setAmount(order.getAmount() - executedsize);
          totalexecutedsize = totalexecutedsize + executedsize;
          registerTrade(order, passiveorder, executedsize);
          orderiterator.remove(); // remove the passive order (last
          // one returned by iterator)
        }
      }
      if (book.get(pricelevel.getKey()).isEmpty()) {
        bookiterator.remove(); // removes price level if orderqueue in
        // it is empty (last one returned by
        // iterator)
      }
    }
    this.updateDepth(order.getSide(), totalexecutedsize);
    this.updateBestPrices();
    return unfilledsize;
  }

  /**
   * Updates best ask/bid
   */
  private void updateBestPrices() {
    val prevbestaks = this.bestAsk;
    val prevbestbid = this.bestBid;
    this.bestAsk = this.asks.isEmpty() ? Float.MAX_VALUE : this.asks.firstKey();
    this.bestBid = this.bids.isEmpty() ? Float.MIN_VALUE : this.bids.firstKey();
    if (this.bestAsk != prevbestaks || this.bestBid != prevbestbid) {
      val quote =
          new Quote(getSimulationTime(), this.getSymbol(), this.getBestAsk(), this.getBestBid(), getDepthAtLevel(
              bestAsk,
              OrderSide.ASK), getDepthAtLevel(bestBid, OrderSide.BID));
      if (!isValidQuote(this.bestBid, this.bestAsk)) {
        log.error("Invalid quote {}", quote);
        return;
      }

      exchange().tell(quote, self());
      publisher().tell(quote, self());
    }
  }

  private int getDepthAtLevel(float price, OrderSide side) {
    int depth = 0;

    NavigableSet<LimitOrder> book;
    if (side == OrderSide.ASK) {
      book = this.asks.get(price);
    } else {
      book = this.bids.get(price);
    }

    if (book != null) {
      for (val order : book) {
        depth = depth + order.getAmount();
      }
    }
    return depth;
  }

  private boolean isValidQuote(float bid, float ask) {
    return ask > bid;
  }

  /**
   * Updates bid depth / ask depth based on executed size
   */
  private void updateDepth(OrderSide orderside, int executedsize) {
    if (orderside == OrderSide.ASK) {
      this.bidDepth = this.bidDepth - executedsize;
    } else {
      this.askDepth = this.askDepth - executedsize;
    }
  }

  /**
   * Checks the validity of the book by inspecting actual depth in the book and comparing it to maintained
   * biddepth/askdepth variables
   */
  private boolean assertBookDepth() {
    int biddepth = 0;
    for (val bidsentries : bids.entrySet()) {
      for (val bids : bidsentries.getValue()) {
        biddepth += bids.getAmount();
      }
    }
    if (biddepth != this.bidDepth) {
      log.error("Bid depth does not add up record = {} actual = {}", this.bidDepth, biddepth);
      return false;
    }
    int askdepth = 0;
    for (val asksentries : asks.entrySet()) {
      for (val asks : asksentries.getValue()) {
        askdepth += asks.getAmount();
      }
    }
    if (askdepth != this.askDepth) {
      log.error("Ask depth does not add up record = {} actual = {}", this.askDepth, askdepth);
      return false;
    }
    return true;
  }

  /**
   * Registers a Trade
   */
  private void registerTrade(Order active, Order passive, int executedsize) {
    tradeCount += 1;
    val trade = new Trade(getSimulationTime(), active, passive, executedsize);
    exchange().tell(trade, self());
    publisher().tell(trade, self());
    if (Seconds.secondsBetween(active.getSentTime(), trade.getTime()).getSeconds() > 5) {
      log.debug("Order took more than 5 seconds to be processed {}", active);
    }
  }

  private void processLimitOrder(LimitOrder order) {
    if (order.getType() == OrderType.AMEND) {
      System.out.println("order amended");
    }
    else if (order.getType() == OrderType.CANCEL) {
      log.debug("cancelling order {}", order);
      this.deleteOrder(order);
    }
    else if (order.getType() == OrderType.ADD) {
      addLimitOrder(order);
    }
    else {
      // TODO: Handle?
    }
  }

  /**
   * Adds LimitOrder to the order book
   */
  private void addLimitOrder(LimitOrder order) {
    int availabledepth = 0;
    if (order.getSide() == OrderSide.ASK) {
      // Executing against bid side of the book.
      availabledepth = this.bidDepth;
    } else {
      availabledepth = this.askDepth;
    }

    int unfilledsize;
    if (crossesSpread(order) && availabledepth > 0) { // if limitprice
      // Crosses spread, treat as market order
      unfilledsize = processMarketOrder(order);
      order.setAmount(unfilledsize);

      // Any unfilled amount added to order book
      if (unfilledsize > 0) {
        insertOrder(order);
      }
    } else {
      // Mot crossing spread or no depth available. So add to limit book
      insertOrder(order);
    }

    if (log.isDebugEnabled()) {
      if (!assertBookDepth()) {
        System.exit(1);
      }
    }
  }

  /**
   * Inserts limit order in the TreeMap<Float,TreeSet<LimitOrder>> data strucuture
   */
  private void insertOrder(LimitOrder order) {
    boolean isBid;
    NavigableMap<Float, NavigableSet<LimitOrder>> sidebook;
    if (order.getSide() == OrderSide.ASK) {
      isBid = false;
      sidebook = this.asks;
    } else {
      isBid = true;
      sidebook = this.bids;
    }

    if (sidebook.isEmpty() || sidebook.get(order.getPrice()) == null) {
      // Add order to order book
      val orderList = new TreeSet<LimitOrder>(LimitOrderTimeComparator.INSTANCE);
      orderList.add(order);
      sidebook.put(order.getPrice(), orderList);
    } else {
      // Order at same price exists, queue it by time
      val orderList = sidebook.get(order.getPrice());
      orderList.add(order);
      sidebook.put(order.getPrice(), orderList);
    }

    // Set best price and depth attributes
    if (isBid) {
      if (this.bidDepth == 0 || order.getPrice() > this.bestBid) {
        this.bestBid = order.getPrice();
      }
      this.bidDepth = this.bidDepth + order.getAmount();
    } else {
      if (this.askDepth == 0 || order.getPrice() < this.bestAsk) {
        this.bestAsk = order.getPrice();
      }
      this.askDepth = this.askDepth + order.getAmount();
    }
    order.setProcessedTime(getSimulationTime());
    if (Seconds.secondsBetween(order.getProcessedTime(), order.getSentTime()).getSeconds() > 5) {
      log.debug("Rrder took more than 5 seconds to be processed: {}", order);
    }

    // publish to tape
    publisher().tell(order, self());

  }

  /**
   * Determines if a limit order crosses the spread i.e. LimitBuy is better priced than bestask or LimitSell is better
   * priced than best bid
   */
  private boolean crossesSpread(LimitOrder order) {
    if (order.getSide() == OrderSide.ASK) {
      if (order.getPrice() <= this.bestBid) {
        return true;
      }
    } else {
      if (order.getPrice() >= this.bestAsk) {
        return true;
      }
    }

    return false;
  }

  /**
   * Deletes an order from this order book.
   */
  // TODO: Untested method.
  private boolean deleteOrder(LimitOrder order) {
    val book = getBook(order);
    val orders = book.get(order.getPrice());

    boolean removed = false;
    if (orders != null) {
      removed = orders.remove(order);

      if (order.getSide() == OrderSide.ASK) {
        this.askDepth = removed ? this.askDepth - order.getAmount() : this.askDepth;
      } else {
        this.bidDepth = removed ? this.bidDepth - order.getAmount() : this.bidDepth;
      }

      publisher().tell(order, self());
    }

    if (log.isDebugEnabled()) {
      if (!assertBookDepth()) {
        System.exit(1);
      }
    }

    return removed;
  }

  private NavigableMap<Float, NavigableSet<LimitOrder>> getBook(LimitOrder order) {
    if (order.getSide() == OrderSide.ASK) {
      return this.bids;
    } else {
      return this.asks;
    }
  }

  public void printBook() {
    log.info(OrderBookFormatter.formatOrderBook(this));
  }

}

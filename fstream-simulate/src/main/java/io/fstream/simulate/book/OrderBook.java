package io.fstream.simulate.book;

import io.fstream.simulate.message.Messages;
import io.fstream.simulate.orders.LimitOrder;
import io.fstream.simulate.orders.Order;
import io.fstream.simulate.orders.Order.OrderSide;
import io.fstream.simulate.orders.Order.OrderType;
import io.fstream.simulate.orders.Quote;
import io.fstream.simulate.orders.Trade;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

/**
 * A price,time ordered implementation of a central limit order book. The principle data structure is a List of IOrders
 * - by definition only limit orders can live in the book. Market orders are accepted and trigger a trade immediately if
 * liquidity is available.
 * 
 * @author bdevani
 *
 */
@Slf4j
@Getter
@Setter
@Component
@Scope("prototype")
public class OrderBook extends UntypedActor {

  TreeMap<Float, TreeSet<LimitOrder>> bids;
  TreeMap<Float, TreeSet<LimitOrder>> asks;
  String symbol;

  float bestbid;
  float bestask;

  int biddepth;
  int askdepth;

  private int ordercount = 0;
  private int tradecount = 0;

  ActorRef exchange;
  ActorRef publisher;

  public OrderBook(String symbol, ActorRef exchange, ActorRef publisher) {
    this.symbol = symbol;
    this.exchange = exchange;
    this.publisher = publisher;
    this.init();
  }

  private void init() {
    this.bids = new TreeMap<Float, TreeSet<LimitOrder>>(Collections.reverseOrder());
    this.asks = new TreeMap<Float, TreeSet<LimitOrder>>();
    this.bestbid = Float.MIN_VALUE;
    this.bestask = Float.MAX_VALUE;
  }

  public void processOrder(Order order) {
    assert (order.getSymbol() == this.symbol);
    ordercount += 1;
    order.setProcessedTime(DateTime.now());
    if (order.getType() == OrderType.MO) { // process market order
      log.debug(String.format("processing market order %s ", order.toString()));
      LimitOrder limitorder = (LimitOrder) order;

      if (order.getSide() == OrderSide.ASK) {
        limitorder.setPrice(Float.MIN_VALUE);
      } else {
        limitorder.setPrice(Float.MAX_VALUE);
      }
      this.processMarketOrder(limitorder);
    } else { // process limit order
      log.debug(String.format("processing limitorder %s ", order.toString()));
      this.processLimitOrder((LimitOrder) order);
    }
  }

  /**
   * Accepts IOrder and executes it against available depth. Returns unfilled amount TODO currently MarketOrders are
   * mimicked via LimitOrders where trigger price is best ask/bid. Need to add marketable order implementation
   * 
   * @param order
   * @return
   */
  private int processMarketOrder(LimitOrder order) {
    TreeMap<Float, TreeSet<LimitOrder>> book;
    if (order.getSide() == OrderSide.ASK) {
      if (this.bids.isEmpty()) {
        log.debug(String.format("No depth. Order not filled %s", order.toString()));
        return order.getAmount();
      }
      book = this.bids;
    } else {
      if (this.asks.isEmpty()) {
        log.debug(String.format("No depth. Order not filled %s", order.toString()));
        return order.getAmount();
      }
      book = this.asks;
    }

    int unfilledsize = order.getAmount();
    int executedsize = 0;
    int totalexecutedsize = 0;
    Iterator<Entry<Float, TreeSet<LimitOrder>>> bookiterator = book.entrySet().iterator();
    while (bookiterator.hasNext()) {
      Entry<Float, TreeSet<LimitOrder>> pricelevel = bookiterator.next();
      Iterator<LimitOrder> orderiterator = pricelevel.getValue().iterator();
      while (orderiterator.hasNext()) {
        LimitOrder passiveorder = orderiterator.next();

        if (unfilledsize <= 0) {
          break;
        }
        if (order.getSide() == OrderSide.ASK) { // limit price exists,
          // respect bounds
          if (order.getPrice() > passiveorder.getPrice()) {
            log.debug(String.format(
                "breaking price crossed on active ASK (SELL) MO for %s orderprice=%s passiveorder=%s",
                this.getSymbol(), order.getPrice(), passiveorder.getPrice()));
            this.updateDepth(order.getSide(), totalexecutedsize);
            this.updateBestPrices();
            return unfilledsize; // price has crossed
          }
        } else {
          if (order.getPrice() < passiveorder.getPrice()) {
            log.debug(String.format(
                "breaking price crossed on active BID (BUY) MO for %s orderprice=%s passiveorder=%s", this.getSymbol(),
                order.getPrice(), passiveorder.getPrice()));
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
   * updates best ask/bid
   */
  private void updateBestPrices() {
    val prevbestaks = this.bestask;
    val prevbestbid = this.bestbid;
    this.bestask = this.asks.isEmpty() ? Float.MAX_VALUE : this.asks.firstKey();
    this.bestbid = this.bids.isEmpty() ? Float.MIN_VALUE : this.bids.firstKey();
    if (this.bestask != prevbestaks || this.bestbid != prevbestbid) {
      val quote =
          new Quote(DateTime.now(), this.getSymbol(), this.getBestask(), this.getBestbid(), getDepthAtLevel(bestask,
              OrderSide.ASK), getDepthAtLevel(bestbid, OrderSide.BID));
      if (!isValidQuote(this.bestbid, this.bestask)) {
        log.info("invalid quote %s", quote.toString());
        return;
      }
      exchange.tell(quote, self());
      publisher.tell(quote, self());
    }
  }

  private int getDepthAtLevel(float price, OrderSide side) {
    int depth = 0;

    TreeSet<LimitOrder> book;
    if (side == OrderSide.ASK) {
      book = this.asks.get(price);
    }
    else {
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
    if (ask <= bid) {
      return false;
    }
    return true;
  }

  /**
   * updates biddepth/askdepth based on executed size
   * 
   * @param orderside
   * @param executedsize
   */
  private void updateDepth(OrderSide orderside, int executedsize) {
    if (orderside == OrderSide.ASK) {
      this.biddepth = this.biddepth - executedsize;
    } else {
      this.askdepth = this.askdepth - executedsize;
    }
  }

  /**
   * Checks the validity of the book by inspecting actual depth in the book and comparing it to maintained
   * biddepth/askdepth variables
   * 
   * @return
   */
  private boolean assertBookDepth() {
    int biddepth = 0;
    for (val bidsentries : bids.entrySet()) {
      for (val bids : bidsentries.getValue()) {
        biddepth += bids.getAmount();
      }
    }
    if (biddepth != this.biddepth) {
      log.error(String.format("bid depth does not add up record = %s actual = %s", this.biddepth, biddepth));
      return false;
    }
    int askdepth = 0;
    for (val asksentries : asks.entrySet()) {
      for (val asks : asksentries.getValue()) {
        askdepth += asks.getAmount();
      }
    }
    if (askdepth != this.askdepth) {
      log.error(String.format("aks depth does not add up record = %s actual = %s", this.askdepth, askdepth));
      return false;
    }
    return true;
  }

  /**
   * Registers a Trade
   * 
   * @param active
   * @param passive
   * @param executedsize
   */
  private void registerTrade(Order active, Order passive, int executedsize) {
    tradecount += 1;
    Trade trade = new Trade(DateTime.now(), active, passive, executedsize);
    exchange.tell(trade, self());
    publisher.tell(trade, self());
    if (Seconds.secondsBetween(active.getSentTime(), trade.getTime()).getSeconds() > 5) {
      log.debug(String.format("order took more than 5 seconds to be processed %s", active.toString()));
    }
  }

  private void processLimitOrder(LimitOrder order) {
    switch (order.getType()) {
    case AMEND:
      System.out.println("order amended");
    case CANCEL:
      System.out.println("order canceled");
    case ADD:
      addLimitOrder(order);
    default:
      // TODO: Handle?
      break;
    }
  }

  /**
   * Adds LimitOrder to the orderbook
   * 
   * @param order
   */
  private void addLimitOrder(LimitOrder order) {
    int availabledepth = 0;
    if (order.getSide() == OrderSide.ASK) {
      // executing against bid side of the book.
      availabledepth = this.biddepth;
    } else {
      availabledepth = this.askdepth;
    }
    int unfilledsize;
    if (crossesSpread(order) && availabledepth > 0) { // if limitprice
      // crosses spread,
      // treat as
      // marketorder

      unfilledsize = processMarketOrder(order);
      order.setAmount(unfilledsize); // any unfilled amount added to
      // orderbook
      if (unfilledsize > 0) {
        insertOrder(order);
      }

    } else { // not crossing spread or no depth available. So add to limit
      // book
      insertOrder(order);
    }

    if (!assertBookDepth()) {
      try {
        throw new Exception("book in invalid state");
      } catch (Exception e) {
        // TODO Auto-generated catch block
        this.printBook();
        e.printStackTrace();
        System.exit(1);
      }
    }
  }

  /**
   * inserts limit order in the TreeMap<Float,TreeSet<LimitOrder>> data strucuture
   * 
   * @param order
   */
  private void insertOrder(LimitOrder order) {
    boolean isBid;
    TreeMap<Float, TreeSet<LimitOrder>> sidebook;
    if (order.getSide() == OrderSide.ASK) {
      isBid = false;
      sidebook = this.asks;
    } else {
      isBid = true;
      sidebook = this.bids;
    }

    if (sidebook.isEmpty() || sidebook.get(order.getPrice()) == null) {
      // add order to order book
      TreeSet<LimitOrder> orderlist = new TreeSet<LimitOrder>(orderTimeComparator);
      orderlist.add(order);
      sidebook.put(order.getPrice(), orderlist);

    } else { // order at same price exists, queue it by time
      TreeSet<LimitOrder> orderlist = sidebook.get(order.getPrice());
      orderlist.add(order);
      sidebook.put(order.getPrice(), orderlist);
    }
    // set best price and depth attributes
    if (isBid) {
      if (this.bestbid == 0 || order.getPrice() > this.bestbid) {
        this.bestbid = order.getPrice();
      }
      this.biddepth = this.biddepth + order.getAmount();
    } else {
      if (this.bestask == 0 || order.getPrice() < this.bestask) {
        this.bestask = order.getPrice();
      }
      this.askdepth = this.askdepth + order.getAmount();
    }
    order.setProcessedTime(DateTime.now());
    if (Seconds.secondsBetween(order.getProcessedTime(), order.getSentTime()).getSeconds() > 5) {
      log.debug(String.format("order took more than 5 seconds to be processed %s", order.toString()));
    }

    // TODO: Add these to the right location
    publisher.tell(order, self());
  }

  /**
   * Determines if a limitorder crosses the spread i.e. LimitBuy is better priced than bestask or LimitSell is better
   * priced than bestbid
   * 
   * @param order
   * @return
   */
  private boolean crossesSpread(LimitOrder order) {
    if (order.getSide() == OrderSide.ASK) {
      if (order.getPrice() <= this.bestbid) {
        return true;
      }
    } else {
      if (order.getPrice() >= this.bestask) {
        return true;
      }
    }
    return false;
  }

  /**
   * deletes an order from orderbook TODO untested method.
   * 
   * @param order
   */
  @SuppressWarnings("unused")
  private boolean deleteOrder(LimitOrder order) {
    TreeMap<Float, TreeSet<LimitOrder>> book;
    if (order.getSide() == OrderSide.ASK) {
      book = this.bids;
    } else {
      book = this.asks;
    }
    return book.get(order.getPrice()).remove(order);

  }

  /**
   * orders limitorders in time priority in the orderbook treeset (Price -> {ordert1, ordert2, ...}
   */
  public static Comparator<LimitOrder> orderTimeComparator = new Comparator<LimitOrder>() {

    @Override
    public int compare(LimitOrder order1, LimitOrder order2) {
      if (order1.equals(order2)) {
        return 0;
      }
      if (order1.getSentTime().getMillis() < order2.getSentTime().getMillis()) {
        return -1;
      } else if (order1.getSentTime().getMillis() > order2.getSentTime().getMillis()) {
        return 1;
      } else {
        return -1; // TODO this is a hack so orders at same time are not
        // ignored. need a better data structure for orders
      }

    }

  };

  public void printBook() {
    log.info(String.format("BOOK = %s", this.getSymbol()));
    String book = new String(String.format("BOOK = %s\n", this.getSymbol()));
    book = book + "------ ASKS -------\n";
    for (val ask : asks.entrySet()) {
      book = book + String.format("%s -> ", ask.getKey());
      for (val firstnode : ask.getValue()) {
        book =
            book
                + String.format("( %s,%s,%s) -> ", firstnode.getSentTime().toString(), firstnode.getPrice(),
                    firstnode.getAmount());
      }
      book = book + "\n";
    }
    book = book + "------ BIDS -------\n";
    for (val bid : bids.entrySet()) {
      book = book + String.format("%s -> ", bid.getKey());
      for (val firstnode : bid.getValue()) {
        book =
            book
                + String.format("( %s,%s,%s) -> ", firstnode.getSentTime().toString(), firstnode.getPrice(),
                    firstnode.getAmount());
      }
      book = book + "\n";
    }
    book = book + String.format("bid depth = %s, ask depth = %s\n", this.biddepth, this.askdepth);
    book =
        book
            + String.format("best ask = %s, best bid =%s, spread = %s\n", this.bestask, this.bestbid, this.bestask
                - this.bestbid);
    book = book + "----- END -----\n";
    log.info(book);
  }

  @Override
  public void onReceive(Object message) throws Exception {
    log.debug("exchange message received " + message.toString());
    if (message instanceof Order) {
      this.processOrder((Order) message);
    } else if (message instanceof String) {
      if (message.equals(Messages.PRINT_ORDER_BOOK)) {
        this.printBook();
        sender().tell(true, self());
      } else if (message.equals(Messages.PRINT_SUMMARY)) {
        log.info(String.format(
            "%s orders processed=%s, trades processed=%s, biddepth=%s, askdepth=%s bestask=%s bestbid=%s spread=%s",
            symbol, ordercount, tradecount, biddepth, askdepth, bestask, bestbid, bestask - bestbid));
      }
    } else {
      unhandled(message);
    }

  }

}

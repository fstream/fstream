package io.fstream.simulate.actor;

import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.message.ActiveInstruments;
import io.fstream.simulate.message.Command;
import io.fstream.simulate.message.QuoteRequest;
import io.fstream.simulate.message.SubscriptionQuote;
import io.fstream.simulate.model.DelayedQuote;
import io.fstream.simulate.model.Order;
import io.fstream.simulate.model.Quote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Setter;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.Props;

@Slf4j
@Setter
public class Exchange extends BaseActor {

  /**
   * Configuration.
   */
  private float minTickSize;
  private FiniteDuration quoteDelayDuration;

  /**
   * State.
   */
  private static AtomicInteger currentOrderId = new AtomicInteger(0);

  private Map<String, ActorRef> orderBooks = new HashMap<>();
  private Map<String, Quote> lastValidQuote = new HashMap<>();

  private List<ActorRef> premiumSubscribers = new ArrayList<>();
  private List<ActorRef> quotesSubscribers = new ArrayList<>();
  private List<ActorRef> quoteAndOrdersSubscribers = new ArrayList<>();

  /**
   * Global order ID generator.
   */
  // TODO: Static method prevents distributed actors across JVMs.
  public static int nextOrderId() {
    return currentOrderId.incrementAndGet();
  }

  public Exchange(SimulateProperties properties) {
    super(properties);
  }

  @Override
  public void preStart() throws Exception {
    activeInstruments.setInstruments(properties.getInstruments());
    minTickSize = properties.getMinTickSize();
    quoteDelayDuration = FiniteDuration.create(properties.getNonPremiumQuoteDelay(), TimeUnit.MILLISECONDS);

    initializeMarketOnOpenQuotes();
  }

  @Override
  public void onReceive(Object message) throws Exception {
    log.debug("Exchange message received {}", message);
    if (message instanceof Order) {
      onReceiveOrder((Order) message);
    } else if (message instanceof Command) {
      onReceiveCommand((Command) message);
    } else if (message instanceof QuoteRequest) {
      onReceiveQuoteRequest((QuoteRequest) message);
    } else if (message instanceof ActiveInstruments) {
      onReceiveActiveInstruments();
    } else if (message instanceof SubscriptionQuote) {
      onReceiveSubscriptionQuote((SubscriptionQuote) message);
    } else if (message instanceof Quote) {
      onReceiveQuote((Quote) message);
    } else {
      unhandled(message);
    }
  }

  private void onReceiveOrder(Order order) {
    if (!activeInstruments.getInstruments().contains(order.getSymbol())) {
      log.error("Order sent for inactive symbol {}", order.getSymbol());
    }

    val orderBook = resolveOrderBook(order.getSymbol());
    orderBook.tell(order, self());
  }

  private void onReceiveQuote(Quote quote) {
    if (quote instanceof DelayedQuote) {
      notifyQuoteAndOrderSubscribers(quote);
      notifyQuoteSubscribers(quote);
    } else {
      lastValidQuote.put(quote.getSymbol(), quote);

      // Notify premium subscribers immediately.
      notifyPremiumSubscribers(quote);

      // Notify non-premium with latency
      val delayedQuote = new DelayedQuote(quote.getTime(), quote.getSymbol(),
          quote.getAskPrice(), quote.getBidPrice(), quote.getAskDepth(),
          quote.getBidDepth());

      scheduleSelfOnce(delayedQuote, quoteDelayDuration);
    }
  }

  private void onReceiveSubscriptionQuote(SubscriptionQuote subscriptionQuote) {
    // TODO: Check to make sure AgentActor is requesting subscription
    val subscription = subscribeForQuote(sender(), subscriptionQuote);
    sender().tell(subscription, self());
  }

  private void onReceiveActiveInstruments() {
    val activeInstruments = new ActiveInstruments(this.activeInstruments.getInstruments());

    sender().tell(activeInstruments, self());
  }

  private void onReceiveQuoteRequest(QuoteRequest quoteRequest) {
    val quote = lastValidQuote.get(quoteRequest.getSymbol());
    sender().tell(quote, self());
  }

  private void onReceiveCommand(Command command) {
    if (command == Command.PRINT_ORDER_BOOK) {
      for (val orderBook : orderBooks.values()) {
        orderBook.tell(Command.PRINT_ORDER_BOOK, self());
      }
    } else if (command == Command.PRINT_SUMMARY) {
      for (val orderBook : orderBooks.values()) {
        orderBook.tell(Command.PRINT_SUMMARY, self());
      }
    }
  }

  private SubscriptionQuote subscribeForQuote(ActorRef agent, SubscriptionQuote message) {
    val level = message.getLevel();

    message.setSuccess(false);

    if (level.equals(Command.SUBSCRIBE_QUOTES.name())) {
      message.setSuccess(this.quotesSubscribers.add(agent));
    } else if (level.equals(Command.SUBSCRIBE_QUOTES_ORDERS.name())) {
      message.setSuccess(this.quoteAndOrdersSubscribers.add(agent));
    } else if (level.equals(Command.SUBSCRIBE_QUOTES_PREMIUM.name())) {
      message.setSuccess(this.premiumSubscribers.add(agent));
    } else {
      log.error("Subscription request not recognized");
    }

    return message;
  }

  /**
   * On market open, initialize quotes to random numbers.
   */
  private void initializeMarketOnOpenQuotes() {
    // TODO: Can we reused the random field instead?
    val random = new Random();

    // TODO remove the hard coding.
    float minBid = 10;
    float minAsk = 12;

    for (val symbol : activeInstruments.getInstruments()) {
      float bid = minBid - (random.nextInt(5) * minTickSize);
      float ask = minAsk + (random.nextInt(5) * minTickSize);
      val quote = new Quote(getSimulationTime(), symbol, ask, bid, 0, 0);

      lastValidQuote.put(symbol, quote);
    }
  }

  private void notifyQuoteSubscribers(Quote quote) {
    for (val agent : quotesSubscribers) {
      agent.tell(quote, self());
    }
  }

  private void notifyQuoteAndOrderSubscribers(Quote quote) {
    for (val agent : quoteAndOrdersSubscribers) {
      agent.tell(quote, self());
    }
  }

  private void notifyPremiumSubscribers(Quote quote) {
    for (val agent : premiumSubscribers) {
      agent.tell(quote, self());
    }
  }

  private ActorRef resolveOrderBook(String symbol) {
    return orderBooks.computeIfAbsent(symbol, (name) -> {
      return context().actorOf(Props.create(OrderBook.class, properties, symbol), name);
    });
  }

}

package io.fstream.simulate.actor;

import io.fstream.core.model.event.Order;
import io.fstream.core.model.event.Quote;
import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.message.ActiveInstruments;
import io.fstream.simulate.message.Command;
import io.fstream.simulate.message.QuoteRequest;
import io.fstream.simulate.message.SubscriptionQuoteRequest;
import io.fstream.simulate.model.DelayedQuote;

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
  private final float minQuoteSize;
  private final FiniteDuration quoteDelayDuration;

  /**
   * State.
   */
  private static final AtomicInteger currentOrderId = new AtomicInteger(0);

  private final Map<String, ActorRef> orderBooks = new HashMap<>();
  private final Map<String, Quote> lastValidQuote = new HashMap<>();

  private final List<ActorRef> premiumSubscribers = new ArrayList<>();
  private final List<ActorRef> quotesSubscribers = new ArrayList<>();
  private final List<ActorRef> quoteAndOrdersSubscribers = new ArrayList<>();

  /**
   * Global order ID generator.
   */
  // TODO: Static method prevents distributed actors across JVMs.
  public static int nextOrderId() {
    return currentOrderId.incrementAndGet();
  }

  public Exchange(SimulateProperties properties) {
    super(properties);
    this.activeInstruments.setInstruments(properties.getInstruments());
    this.minQuoteSize = properties.getMinQuoteSize();
    this.quoteDelayDuration = FiniteDuration.create(properties.getNonPremiumQuoteDelay(), TimeUnit.MILLISECONDS);
  }

  @Override
  public void preStart() throws Exception {
    // On market open, initialize quotes to random numbers.

    // TODO remove the hard coding.
    val random = new Random();
    val minBid = 10;
    val minAsk = 12;

    for (val symbol : activeInstruments.getInstruments()) {
      val bid = minBid - (random.nextInt(5) * minQuoteSize);
      val ask = minAsk + (random.nextInt(5) * minQuoteSize);
      val quote = new Quote(getSimulationTime(), symbol, ask, bid, 0, 0);

      lastValidQuote.put(symbol, quote);
    }
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
    } else if (message instanceof SubscriptionQuoteRequest) {
      onReceiveSubscriptionQuoteRequest((SubscriptionQuoteRequest) message);
    } else if (message instanceof Quote) {
      onReceiveQuote((Quote) message);
    } else {
      unhandled(message);
    }
  }

  private void onReceiveOrder(Order order) {
    val symbol = order.getSymbol();
    if (!isActiveInstrument(symbol)) {
      log.error("Order sent for inactive symbol {}", symbol);
    }

    val orderBook = resolveOrderBook(symbol);
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
      val delayedQuote = new DelayedQuote(quote.getDateTime(), quote.getSymbol(),
          quote.getAsk(), quote.getBid(), quote.getAskAmount(),
          quote.getBidAmount());

      scheduleSelfOnce(delayedQuote, quoteDelayDuration);
    }
  }

  private void onReceiveSubscriptionQuoteRequest(SubscriptionQuoteRequest subscriptionQuote) {
    // TODO: Check to make sure Agent is requesting subscription
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
    val recognized = command == Command.PRINT_ORDER_BOOK || command == Command.PRINT_SUMMARY;
    if (recognized) {
      for (val orderBook : orderBooks.values()) {
        orderBook.tell(command, self());
      }
    }
  }

  private SubscriptionQuoteRequest subscribeForQuote(ActorRef agent, SubscriptionQuoteRequest message) {
    val level = message.getLevel();
    if (level.equals(Command.SUBSCRIBE_QUOTES.name())) {
      message.setSuccess(this.quotesSubscribers.add(agent));
    } else if (level.equals(Command.SUBSCRIBE_QUOTES_ORDERS.name())) {
      message.setSuccess(this.quoteAndOrdersSubscribers.add(agent));
    } else if (level.equals(Command.SUBSCRIBE_QUOTES_PREMIUM.name())) {
      message.setSuccess(this.premiumSubscribers.add(agent));
    } else {
      message.setSuccess(false);
      log.error("Subscription request not recognized");
    }

    return message;
  }

  private void notifyQuoteSubscribers(Quote quote) {
    notifyAgents(quote, quotesSubscribers);
  }

  private void notifyQuoteAndOrderSubscribers(Quote quote) {
    notifyAgents(quote, quoteAndOrdersSubscribers);
  }

  private void notifyPremiumSubscribers(Quote quote) {
    notifyAgents(quote, premiumSubscribers);
  }

  private void notifyAgents(Quote quote, List<ActorRef> agents) {
    agents.stream().forEach(agent -> agent.tell(quote, self()));
  }

  private ActorRef resolveOrderBook(String symbol) {
    // Create book on demand, as needed
    return orderBooks.computeIfAbsent(symbol, (name) -> {
      Props props = Props.create(OrderBook.class, properties, symbol);
      return context().actorOf(props, name);
    });
  }

}

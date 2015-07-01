package io.fstream.simulate.actor;

import io.fstream.simulate.message.ActiveInstruments;
import io.fstream.simulate.message.Command;
import io.fstream.simulate.message.QuoteRequest;
import io.fstream.simulate.message.SubscriptionQuote;
import io.fstream.simulate.model.DelayedQuote;
import io.fstream.simulate.model.Order;
import io.fstream.simulate.model.Quote;
import io.fstream.simulate.util.SingletonActor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;

import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;

@Slf4j
@Setter
@SingletonActor
@RequiredArgsConstructor
public class Exchange extends BaseActor {

  /**
   * Dependencies.
   */
  @NonNull
  private final ActorRef publisher;

  /**
   * Configuration.
   */
  private float minTickSize;
  private ActiveInstruments activeInstruments = new ActiveInstruments();
  private FiniteDuration quoteDelayDuration;

  /**
   * State.
   */
  private static AtomicInteger currentOrderId = new AtomicInteger(0);

  private Map<String, ActorRef> processors = new HashMap<>();
  private List<ActorRef> premiumSubscribers = new ArrayList<>();
  private List<ActorRef> quotesSubscribers = new ArrayList<>();
  private List<ActorRef> quoteAndOrdersSubscribers = new ArrayList<>();

  private Map<String, Quote> lastValidQuote;

  @PostConstruct
  public void init() {
    activeInstruments.setInstruments(properties.getInstruments());
    minTickSize = properties.getMinTickSize();
    quoteDelayDuration = FiniteDuration.create(properties.getNonPremiumQuoteDelay(), TimeUnit.MILLISECONDS);

    initializeMarketOnOpenQuotes();
  }

  /**
   * Global order ID generator.
   */
  // TODO: Prevents distributed actors
  public static int nextOrderId() {
    return currentOrderId.incrementAndGet();
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
      onActiveInstruments();
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

    val processor = getProcessor(order.getSymbol());
    processor.tell(order, self());
  }

  private void onReceiveQuote(Quote quote) {
    if (quote instanceof DelayedQuote) {
      notifyQuoteAndOrderSubscribers(quote);
      notifyQuoteSubscribers(quote);
    } else {
      lastValidQuote.put(quote.getSymbol(), quote);

      // Notify premium subscribers immediately.
      notifyPremiumSubscribers(quote);

      // Notify non-premium with latency.
      val delayedQuote = new DelayedQuote(quote.getTime(), quote.getSymbol(),
          quote.getAskPrice(), quote.getBidPrice(), quote.getAskDepth(),
          quote.getBidDepth());

      // Schedule a DelayedQuote message to self
      scheduleOnce(delayedQuote, quoteDelayDuration);
    }
  }

  private void onReceiveSubscriptionQuote(SubscriptionQuote subscriptionQuote) {
    // TODO: Check to make sure AgentActor is requesting subscription
    val subscription = subscribeForQuote(sender(), subscriptionQuote);
    sender().tell(subscription, self());
  }

  private void onActiveInstruments() {
    val activeInstruments = new ActiveInstruments(this.activeInstruments.getInstruments());

    sender().tell(activeInstruments, self());
  }

  private void onReceiveQuoteRequest(QuoteRequest quoteRequest) {
    val quote = lastValidQuote.get(quoteRequest.getSymbol());
    sender().tell(quote, self());
  }

  private void onReceiveCommand(Command command) {
    if (command == Command.PRINT_ORDER_BOOK) {
      for (val processor : processors.entrySet()) {
        processor.getValue().tell(Command.PRINT_ORDER_BOOK, self());
      }
    } else if (command == Command.PRINT_SUMMARY) {
      for (val processor : processors.entrySet()) {
        processor.getValue().tell(Command.PRINT_SUMMARY, self());
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
      log.error("subscription request not recognized");
    }

    return message;
  }

  /**
   * On market open, initialize quotes to random numbers.
   */
  private void initializeMarketOnOpenQuotes() {
    this.lastValidQuote = new HashMap<String, Quote>();

    val random = new Random();
    // TODO remove the hard coding.
    float minBid = 10;
    float minAsk = 12;

    for (val symbol : activeInstruments.getInstruments()) {
      float bid = minBid - (random.nextInt(5) * minTickSize);
      float ask = minAsk + (random.nextInt(5) * minTickSize);
      val quote = new Quote(DateTime.now(), symbol, ask, bid, 0, 0);

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

  private ActorRef getProcessor(String instrument) {
    val maybeProcessor = processors.get(instrument);
    if (maybeProcessor != null) {
      return maybeProcessor;
    }

    val processor = context().actorOf(spring.props(OrderBook.class, instrument, self(), publisher), instrument);
    processors.put(instrument, processor);

    return processor;
  }

}

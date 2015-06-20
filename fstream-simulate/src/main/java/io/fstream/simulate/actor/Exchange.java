package io.fstream.simulate.actor;

import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.message.ActiveInstruments;
import io.fstream.simulate.message.Messages;
import io.fstream.simulate.message.QuoteRequest;
import io.fstream.simulate.message.SubscriptionQuote;
import io.fstream.simulate.model.DelayedQuote;
import io.fstream.simulate.model.Order;
import io.fstream.simulate.model.Quote;
import io.fstream.simulate.util.SingletonActor;
import io.fstream.simulate.util.SpringExtension;

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
import org.springframework.beans.factory.annotation.Autowired;

import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

@Slf4j
@Setter
@SingletonActor
@RequiredArgsConstructor
public class Exchange extends UntypedActor {

  /**
   * Dependencies.
   */
  @NonNull
  private final ActorRef publisher;

  @Autowired
  private SimulateProperties properties;
  @Autowired
  private SpringExtension spring;

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

  private Map<String, ActorRef> processors = new HashMap<String, ActorRef>();
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
      lastValidQuote.put(symbol, new Quote(DateTime.now(), symbol, ask, bid, 0, 0));
    }
  }

  public static int nextOrderId() {
    return currentOrderId.incrementAndGet();
  }

  @Override
  // TODO a more elegant message parser rather than a giant if statement
  public void onReceive(Object message) throws Exception {
    log.debug("exchange message received " + message.toString());
    if (message instanceof Order) {
      if (!activeInstruments.getInstruments().contains(((Order) message).getSymbol())) {
        log.error("Order sent for inactive symbol {}", ((Order) message).getSymbol());
      } else {
        dispatch((Order) message);
      }
    } else if (message instanceof String) {
      if (message.equals(Messages.PRINT_ORDER_BOOK)) {
        for (val processor : processors.entrySet()) {
          processor.getValue().tell(Messages.PRINT_ORDER_BOOK, self());
        }
      } else if (message.equals(Messages.PRINT_SUMMARY)) {
        for (val processor : processors.entrySet()) {
          processor.getValue().tell(Messages.PRINT_SUMMARY, self());
        }
      }
      else if (message.equals(Messages.GET_MARKET_OPEN_QUOTE)) {
        sender().tell(lastValidQuote.get(((QuoteRequest) message).getSymbol()), self());
      }
    } else if (message instanceof ActiveInstruments) {
      // TODO implement clone method
      ActiveInstruments activeinstrument = new ActiveInstruments();
      activeinstrument.setInstruments(this.activeInstruments.getInstruments());
      sender().tell(activeinstrument, self());
    } else if (message instanceof SubscriptionQuote) {
      // TODO check to make sure AgentActor is requesting subscription
      sender().tell(subscribeForQuote(sender(), (SubscriptionQuote) message), self());
    } else if (message instanceof Quote && message.getClass() != DelayedQuote.class) {
      lastValidQuote.put(((Quote) message).getSymbol(), (Quote) message);
      // notify premium subscribers immediately.
      notifyPremiumSubscribers((Quote) message);
      // notify non-premium with latency. Schedule a DelayedQuote message to self
      DelayedQuote dQuote =
          new DelayedQuote(((Quote) message).getTime(), ((Quote) message).getSymbol(), ((Quote) message).getAskprice(),
              ((Quote) message).getBidprice(), ((Quote) message).getAskdepth(), ((Quote) message).getBiddepth());
      getContext()
          .system()
          .scheduler()
          .scheduleOnce(quoteDelayDuration, getSelf(), dQuote, getContext().dispatcher(),
              null);
    } else if (message instanceof DelayedQuote) {
      notifyQuoteAndOrderSubscribers((Quote) message);
      notifyQuoteSubscribers((Quote) message);
    } else if (message instanceof QuoteRequest) {
      Quote quote = lastValidQuote.get(((QuoteRequest) message).getSymbol());
      sender().tell(quote, self());
    } else {
      unhandled(message);
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

  private void dispatch(Order order) {
    val processor = getProcessor(order.getSymbol());
    processor.tell(order, self());
  }

  private ActorRef getProcessor(String instrument) {
    final ActorRef maybeProcessor = processors.get(instrument);
    if (maybeProcessor == null) {
      val processor = context().actorOf(spring.props(OrderBook.class, instrument, self(), publisher), instrument);

      processors.put(instrument, processor);
      return processor;
    }

    return maybeProcessor;
  }

  public SubscriptionQuote subscribeForQuote(ActorRef agent, SubscriptionQuote message) {
    message.setSuccess(false);
    if (message.getLevel() == Messages.SUBSCRIBE_QUOTES) {
      message.setSuccess(this.quotesSubscribers.add(agent));
    } else if (message.getLevel() == Messages.SUBSCRIBE_QUOTES_ORDERS) {
      message.setSuccess(this.quotesSubscribers.add(agent));
    } else if (message.getLevel() == Messages.SUBSCRIBE_QUOTES_PREMIUM) {
      message.setSuccess(this.quotesSubscribers.add(agent));
    }

    return message;
  }

}

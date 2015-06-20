package io.fstream.simulate.agent;

import io.fstream.simulate.book.OrderBook;
import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.message.ActiveInstruments;
import io.fstream.simulate.message.Messages;
import io.fstream.simulate.message.QuoteRequest;
import io.fstream.simulate.message.SubscriptionQuote;
import io.fstream.simulate.orders.DelayedQuote;
import io.fstream.simulate.orders.Order;
import io.fstream.simulate.orders.Quote;
import io.fstream.simulate.spring.SpringExtension;

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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

@Slf4j
@Lazy
@Component
@Setter
@RequiredArgsConstructor
public class Exchange extends UntypedActor {

  /**
   * Dependencies.
   */
  @NonNull
  private final ActorRef publisher;

  @Autowired
  private SpringExtension spring;
  @Autowired
  private SimulateProperties properties;

  /**
   * Configuration.
   */
  private float minTickSize;
  private ActiveInstruments activeinstruments = new ActiveInstruments();
  private FiniteDuration quoteDelayDuration;

  /**
   * State.
   */
  private static AtomicInteger OID = new AtomicInteger(0);
  private Map<String, ActorRef> processors = new HashMap<String, ActorRef>();
  private List<ActorRef> premiumSubscribers = new ArrayList<>();
  private List<ActorRef> quotesSubscribers = new ArrayList<>();
  private List<ActorRef> quoteAndOrdersSubscribers = new ArrayList<>();
  private Map<String, Quote> lastValidQuote;

  public ActorRef getOrderBook(String instrument) {
    return processors.get(instrument);
  }

  @PostConstruct
  public void init() {
    activeinstruments.setActiveinstruments(properties.getInstruments());
    minTickSize = properties.getMinTickSize();
    quoteDelayDuration = FiniteDuration.create(properties.getNonPremiumQuoteDelay(), TimeUnit.MILLISECONDS);

    initializeMarketOnOpenQuotes();
  }

  /**
   * On market open, initialize quotes to random numbers.
   */
  private void initializeMarketOnOpenQuotes() {
    this.lastValidQuote = new HashMap<String, Quote>();
    Random random = new Random();
    // TODO remove the hard coding.
    float minBid = 10;
    float minAsk = 12;
    for (val symbol : activeinstruments.getActiveinstruments()) {
      float bid = minBid - (random.nextInt(5) * minTickSize);
      float ask = minAsk + (random.nextInt(5) * minTickSize);
      lastValidQuote.put(symbol, new Quote(DateTime.now(), symbol, ask, bid, 0, 0));
    }
  }

  public synchronized static int getOID() {
    return OID.incrementAndGet();
  }

  @Override
  // TODO a more elegant message parser rather than a giant if statement
  public void onReceive(Object message) throws Exception {
    log.debug("exchange message received " + message.toString());
    if (message instanceof Order) {
      if (!activeinstruments.getActiveinstruments().contains(((Order) message).getSymbol())) {
        log.error(String.format("order sent for inactive symbol %s", ((Order) message).getSymbol()));
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
      activeinstrument.setActiveinstruments(this.activeinstruments.getActiveinstruments());
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
    }
    else if (message instanceof DelayedQuote) {
      notifyQuoteAndOrderSubscribers((Quote) message);
      notifyQuoteSubscribers((Quote) message);
    }
    else if (message instanceof QuoteRequest) {
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
    final ActorRef processor = getProcessor(order.getSymbol());
    processor.tell(order, self());
  }

  private ActorRef getProcessor(String instrument) {
    final ActorRef maybeprocessor = processors.get(instrument);
    if (maybeprocessor == null) {

      val processor = context().actorOf(spring.props(OrderBook.class, instrument, self(), publisher), instrument);

      processors.put(instrument, processor);
      return processor;
    }
    return maybeprocessor;
  }

  public SubscriptionQuote subscribeForQuote(ActorRef agent, SubscriptionQuote message) {
    message.setSuccess(false);
    if (message.getLevel() == Messages.SUBSCRIBE_QUOTES) {
      message.setSuccess(this.quotesSubscribers.add(agent));
    }
    else if (message.getLevel() == Messages.SUBSCRIBE_QUOTES_ORDERS) {
      message.setSuccess(this.quotesSubscribers.add(agent));
    }
    else if (message.getLevel() == Messages.SUBSCRIBE_QUOTES_PREMIUM) {
      message.setSuccess(this.quotesSubscribers.add(agent));
    }
    return message;
  }
}

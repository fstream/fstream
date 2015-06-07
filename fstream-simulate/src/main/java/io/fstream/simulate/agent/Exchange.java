package io.fstream.simulate.agent;

import io.fstream.simulate.book.OrderBook;
import io.fstream.simulate.book.TradeBook;
import io.fstream.simulate.config.SimulateProperties;
import io.fstream.simulate.message.ActiveInstruments;
import io.fstream.simulate.message.BbBo;
import io.fstream.simulate.message.Messages;
import io.fstream.simulate.message.State;
import io.fstream.simulate.orders.Order;
import io.fstream.simulate.orders.Trade;
import io.fstream.simulate.spring.SpringExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

@Slf4j
@Lazy
// This is required otherwise Akka will complain it was created outside of a
// factory
@Component
public class Exchange extends UntypedActor {

  private static AtomicInteger OID = new AtomicInteger(0);

  @Autowired
  private SpringExtension spring;
  @Autowired
  private SimulateProperties properties;

  private HashMap<String, ActorRef> processors;
  private ActorRef tradebook;
  ActiveInstruments activeinstruments;

  public ActorRef getOrderBook(String instrument) {
    return processors.get(instrument);
  }

  @PostConstruct
  public void init() {
    activeinstruments = new ActiveInstruments();
    activeinstruments.setActiveinstruments(Arrays.asList("RY", "BBM",
        "BMO", "TD", "CIBC", "HUF"));
    tradebook = context().actorOf(spring.props(TradeBook.class),
        "tradebook");
    processors = new HashMap<String, ActorRef>();
  }

  public synchronized static int getOID() {
    return OID.incrementAndGet();
  }

  @Override
  // TODO a more elegant message parser rather than a giant if statement
  public void onReceive(Object message) throws Exception {
    log.debug("exchange message received " + message.toString());
    if (message instanceof Order) {
      message = (Order) message;
      if (!activeinstruments.getActiveinstruments().contains(
          ((Order) message).getSymbol())) {
        log.error(String.format("order sent for inactive symbol %s",
            ((Order) message).getSymbol()));
      } else {
        dispatch((Order) message);
      }

    } else if (message instanceof State) {
      State state = (State) message;
      ActorRef processor = getProcessor(state.getSymbol());
      processor.tell(state, sender());

    } else if (message instanceof BbBo) {
      BbBo bbbo = (BbBo) message;
      ActorRef processor = getProcessor(bbbo.getSymbol());
      processor.tell(bbbo, sender());

    } else if (message instanceof Trade) {
      tradebook.tell(message, self());
    } else if (message instanceof String) {
      message = (String) message;
      if (message.equals(Messages.PRINT_ORDER_BOOK)) {
        for (val processor : processors.entrySet()) {
          processor.getValue()
              .tell(Messages.PRINT_ORDER_BOOK, self());
        }
      } else if (message.equals(Messages.PRINT_TRADE_BOOK)) {
        tradebook.tell(Messages.PRINT_TRADE_BOOK, self());
      } else if (message.equals(Messages.PRINT_SUMMARY)) {
        for (val processor : processors.entrySet()) {
          processor.getValue().tell(Messages.PRINT_SUMMARY, self());
        }
      }
    } else if (message instanceof ActiveInstruments) {
      // TODO implement clone method
      ActiveInstruments activeinstrument = new ActiveInstruments();
      activeinstrument.setActiveinstruments(this.activeinstruments
          .getActiveinstruments());
      sender().tell(activeinstrument, self());
    } else {
      unhandled(message);
    }
  }

  private void dispatch(Order order) {
    final ActorRef processor = getProcessor(order.getSymbol());
    processor.tell(order, self());
  }

  private ActorRef getProcessor(String instrument) {
    final ActorRef maybeprocessor = (ActorRef) processors.get(instrument);
    if (maybeprocessor == null) {
      // final ActorRef processor =
      // context().actorOf(Props.create(OrderBook.class,instrument,self()),
      // instrument);
      val processor = context().actorOf(
          spring.props(OrderBook.class, instrument, self()),
          instrument);

      processors.put(instrument, processor);
      return processor;
    }
    return maybeprocessor;
  }
}

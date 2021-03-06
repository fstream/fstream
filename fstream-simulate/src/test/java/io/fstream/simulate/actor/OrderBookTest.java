package io.fstream.simulate.actor;

import static io.fstream.core.model.event.Order.OrderSide.ASK;
import static io.fstream.core.model.event.Order.OrderSide.BID;
import static io.fstream.core.model.event.Order.OrderType.LIMIT_ADD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import io.fstream.core.model.event.Order;
import io.fstream.simulate.actor.publisher.CamelPublisher;
import io.fstream.simulate.config.SimulateProperties;
import lombok.val;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;

public class OrderBookTest {

  /**
   * Class under test.
   */
  OrderBook orderBook;

  /**
   * Dependencies.
   */
  ActorSystem actorSystem;

  /**
   * Test state.
   */
  final DateTime time = DateTime.now();
  final String symbol = "RY";
  final String brokerId = "tsx";

  @Before
  public void setUp() throws Exception {
    this.actorSystem = ActorSystem.create("fstream-simulate-test");

    // Create shared configuration
    val properties = new SimulateProperties();

    // Create the publisher
    val publisheProps = Props.create(CamelPublisher.class, "log:info");
    TestActorRef.<CamelPublisher> create(actorSystem, publisheProps, "publisher");

    // Create the exchange
    val exchangeProps = Props.create(Exchange.class, properties);
    TestActorRef.<BaseActor> create(actorSystem, exchangeProps, "exchange");

    // Create the order book
    val orderBookProps = Props.create(OrderBook.class, properties, symbol);
    val orderBookRef = TestActorRef.<OrderBook> create(actorSystem, orderBookProps, symbol);
    this.orderBook = orderBookRef.underlyingActor();
  }

  @After
  public void tearDown() {
    // Ensure orderly shutdown
    actorSystem.shutdown();
  }

  @Test
  public void testAddOrders() throws Exception {
    val ask1 = new Order(ASK, LIMIT_ADD, time, 1, brokerId, symbol, 1000, 25f, "u1");
    val ask2 = new Order(ASK, LIMIT_ADD, time.plusMinutes(1), 2, brokerId, symbol, 1000, 26f, "u2");
    val ask3 = new Order(ASK, LIMIT_ADD, time.plusMinutes(2), 3, brokerId, symbol, 1000, 24f, "u3");
    val ask4 = new Order(ASK, LIMIT_ADD, time.plusMinutes(3), 4, brokerId, symbol, 1000, 24f, "u4");

    val bid5 = new Order(ASK, LIMIT_ADD, time.plus(4), 4, brokerId, symbol, 1000, 19f, "u5");
    val bid6 = new Order(ASK, LIMIT_ADD, time.plusMinutes(5), 5, brokerId, symbol, 1000, 19f, "u6");
    val bid7 = new Order(ASK, LIMIT_ADD, time.plusMinutes(6), 6, brokerId, symbol, 1000, 18f, "u7");

    orderBook.onReceive(ask1);
    assertTrue(orderBook.getAsks().getPriceLevelCount() == 1);
    assertTrue(orderBook.getAsks().getDepth() == 1000);
    assertThat(orderBook.getBestAsk()).isEqualTo(25f);

    orderBook.onReceive(ask2);
    assertTrue(orderBook.getAsks().getPriceLevelCount() == 2);
    assertTrue(orderBook.getAsks().getDepth() == 2000);
    assertTrue(orderBook.getBestAsk() == 25f);

    orderBook.onReceive(ask3);
    assertTrue(orderBook.getAsks().getPriceLevelCount() == 3);
    assertTrue(orderBook.getAsks().getDepth() == 3000);
    assertTrue(orderBook.getBestAsk() == 24f);

    orderBook.onReceive(ask4);
    assertTrue(orderBook.getAsks().getPriceLevelCount() == 3);
    assertTrue(orderBook.getAsks().getDepth() == 4000);
    assertTrue(orderBook.getBestAsk() == 24f);
    assertTrue(orderBook.getAsks().getPriceLevel(24f).size() == 2);
    Order[] orderlist = orderBook.getAsks().getPriceLevel(24f).toArray(new Order[0]);
    assertTrue(orderlist.length == 2);
    assertTrue(orderlist[0].getDateTime().getMillis() <= orderlist[1].getDateTime().getMillis());

    orderBook.onReceive(bid5);
    orderBook.onReceive(bid6);
    orderBook.onReceive(bid7);
  }

  @Test
  public void addLimitTest2() throws Exception {
    val ask0 = new Order(ASK, LIMIT_ADD, time, 1, brokerId, symbol, 91, 10.277141f, "u1");
    val bid1 = new Order(BID, LIMIT_ADD, time.plusMinutes(1), 1, brokerId, symbol, 36, 6.828021f, "u1");
    val bid2 = new Order(BID, LIMIT_ADD, time.plusMinutes(2), 1, brokerId, symbol, 63, 6.5065575f, "u1");
    val bid3 = new Order(BID, LIMIT_ADD, time.plusMinutes(3), 1, brokerId, symbol, 51, 3.4671168f, "u1");
    val bid4 = new Order(BID, LIMIT_ADD, time.plusMinutes(4), 1, brokerId, symbol, 35, 4.0023165f, "u1");
    val bid5 = new Order(BID, LIMIT_ADD, time.plusMinutes(5), 1, brokerId, symbol, 47, 5.373663f, "u1");
    val bid6 = new Order(BID, LIMIT_ADD, time.plusMinutes(6), 1, brokerId, symbol, 52, 10.277141f, "u1");

    orderBook.onReceive(ask0);
    orderBook.onReceive(bid1);
    orderBook.onReceive(bid2);
    orderBook.onReceive(bid3);
    orderBook.onReceive(bid4);
    orderBook.onReceive(bid5);
    orderBook.onReceive(bid6);

    // ob.printBook();
  }

  @Test
  public void addLimitTest3() throws Exception {
    // processing 2015-05-20T10:29:05.506-04:00,34,7.9161587,BID,hft1
    // processing 2015-05-20T10:29:05.506-04:00,89,10.97681,ASK,hft2
    // processing 2015-05-20T10:29:05.647-04:00,90,7.9161587,ASK,hft1
    // Trade registered for active ASK of 34 at price 7.9161587
    // processing 2015-05-20T10:29:05.647-04:00,84,7.9161587,ASK,hft2
    // ask depth does not add up record = 229 actual =145

    val bid0 = new Order(BID, LIMIT_ADD, time, 0, brokerId, symbol, 34, 7.9161587f, "u1");
    val bid1 = new Order(BID, LIMIT_ADD, time, 1, brokerId, symbol, 50, 7.9161587f, "u1");
    val ask1 = new Order(ASK, LIMIT_ADD, time.plusMinutes(1), 2, brokerId, symbol, 89, 10.97681f, "u1");
    val ask2 = new Order(ASK, LIMIT_ADD, time.plusMinutes(2), 3, brokerId, symbol, 90, 7.9161587f, "u1");
    val ask3 = new Order(ASK, LIMIT_ADD, time.plusMinutes(3), 4, brokerId, symbol, 84, 7.9161587f, "u1");

    orderBook.onReceive(bid0);
    orderBook.onReceive(bid1);
    orderBook.onReceive(ask1);
    orderBook.onReceive(ask2);
    orderBook.onReceive(ask3);

    orderBook.printBook();
  }

  @Test
  public void addLimitTest4() throws Exception {
    // processing 2015-05-20T21:02:27.593-04:00,7,6.180016,BID,hft2
    // processing 2015-05-20T21:02:27.593-04:00,9,10.0,BID,hft1
    // processing 2015-05-20T21:02:27.740-04:00,55,10.0,BID,hft2
    // processing 2015-05-20T21:02:27.741-04:00,83,10.0,ASK,hft1

    val bid1 = new Order(BID, LIMIT_ADD, time, 1, brokerId, symbol, 7, 6.180016f, "u1");
    val bid2 = new Order(BID, LIMIT_ADD, time, 2, brokerId, symbol, 9, 10.0f, "u1");
    val bid3 = new Order(BID, LIMIT_ADD, time.plusMinutes(1), 3, brokerId, symbol, 55, 10.0f, "u1");
    val ask1 = new Order(ASK, LIMIT_ADD, time.plusMinutes(2), 4, brokerId, symbol, 83, 10.0f, "u1");

    orderBook.onReceive(bid1);
    orderBook.onReceive(bid2);
    orderBook.onReceive(bid3);
    orderBook.onReceive(ask1);

    orderBook.printBook();
  }

  @Test
  public void addLimitTest5() throws Exception {
    // processing 2015-05-20T22:19:11.145-04:00,1,47,7.5659113,BID,hft2
    // processing 2015-05-20T22:19:11.145-04:00,2,48,10.0,BID,hft1
    // processing 2015-05-20T22:19:11.284-04:00,3,35,10.0,BID,hft1
    // processing 2015-05-20T22:19:11.284-04:00,4,32,10.0,BID,hft2

    val bid1 = new Order(BID, LIMIT_ADD, time, 1, brokerId, symbol, 47, 7.5659113f, "u1");
    val bid2 = new Order(BID, LIMIT_ADD, time, 2, brokerId, symbol, 48, 10.0f, "u1");
    val bid3 = new Order(BID, LIMIT_ADD, time.plusMinutes(1), 3, brokerId, symbol, 35, 10.0f, "u1");
    val bid4 = new Order(BID, LIMIT_ADD, time.plusMinutes(1), 4, brokerId, symbol, 32, 10.0f, "u1");

    orderBook.onReceive(bid1);
    orderBook.onReceive(bid2);
    orderBook.onReceive(bid3);
    orderBook.onReceive(bid4);

    orderBook.printBook();
  }

  @Test
  public void addLimitTest6() throws Exception {
    // processing 2015-05-21T12:48:20.437-04:00,1,81,8.0,ASK,hft2
    // processing 2015-05-21T12:48:20.437-04:00,2,91,11.686356,ASK,hft1
    // processing 2015-05-21T12:48:20.574-04:00,3,98,8.0,ASK,hft2
    // processing 2015-05-21T12:48:20.574-04:00,4,32,8.0,BID,hft1

    val ask1 = new Order(ASK, LIMIT_ADD, time, 1, brokerId, symbol, 81, 8.0f, "u1");
    val ask2 = new Order(ASK, LIMIT_ADD, time, 2, brokerId, symbol, 91, 11.686356f, "u1");
    val ask3 = new Order(ASK, LIMIT_ADD, time.plusMinutes(1), 3, brokerId, symbol, 98, 8.0f, "u1");
    val bid1 = new Order(BID, LIMIT_ADD, time.plusMinutes(1), 4, brokerId, symbol, 32, 8.0f, "u1");

    orderBook.onReceive(ask1);
    orderBook.onReceive(ask2);
    orderBook.onReceive(ask3);
    orderBook.onReceive(bid1);

    orderBook.printBook();
  }

  @Test
  public void addLimitTest7() throws Exception {
    // TODO: Add asserts!
    // processing 2015-05-21T15:53:21.206-04:00,1,50,7.8310976,BID,hft1
    // processing 2015-05-21T15:53:21.206-04:00,2,67,6.1404834,BID,hft2
    // processing 2015-05-21T15:53:21.344-04:00,3,63,10.0,BID,hft1
    // processing 2015-05-21T15:53:21.344-04:00,4,9,6.307989,BID,hft2
    // processing 2015-05-21T15:53:21.447-04:00,5,76,10.0,BID,hft2
    // processing 2015-05-21T15:53:21.447-04:00,6,15,10.0,BID,hft1
    // processing 2015-05-21T15:53:21.548-04:00,7,2,10.0,BID,hft2
    // processing 2015-05-21T15:53:21.548-04:00,8,32,10.0,BID,hft1
    // processing 2015-05-21T15:53:21.653-04:00,9,12,10.0,BID,hft2
    // processing 2015-05-21T15:53:21.653-04:00,10,95,10.0,ASK,hft1
    // Trade registered for active ASK of 63 at price 10.0
    // Trade registered for active ASK of 78 at price 10.0
    // Trade registered for active ASK of 173 at price 10.0
    // bid depth does not add up record = 153 actual =231

    val bid1 = new Order(BID, LIMIT_ADD, time, 1, brokerId, symbol, 50, 7.8310976f, "u1");
    val bid2 = new Order(BID, LIMIT_ADD, time, 2, brokerId, symbol, 67, 6.1404834f, "u1");
    val bid3 = new Order(BID, LIMIT_ADD, time.plusMinutes(1), 3, brokerId, symbol, 63, 10.0f, "u1");
    val bid4 = new Order(BID, LIMIT_ADD, time.plusMinutes(1), 4, brokerId, symbol, 9, 6.307989f, "u1");
    val bid5 = new Order(BID, LIMIT_ADD, time.plusMinutes(2), 5, brokerId, symbol, 76, 10.0f, "u1");
    val bid6 = new Order(BID, LIMIT_ADD, time.plusMinutes(2), 6, brokerId, symbol, 15, 10.0f, "u1");
    val bid7 = new Order(BID, LIMIT_ADD, time.plusMinutes(3), 7, brokerId, symbol, 2, 10.0f, "u1");
    val bid8 = new Order(BID, LIMIT_ADD, time.plusMinutes(3), 8, brokerId, symbol, 32, 10.0f, "u1");
    val bid9 = new Order(BID, LIMIT_ADD, time.plusMinutes(4), 9, brokerId, symbol, 12, 10.0f, "u1");
    val ask1 = new Order(ASK, LIMIT_ADD, time.plusMinutes(4), 11, brokerId, symbol, 95, 10.0f, "u1");

    orderBook.onReceive(bid1);
    orderBook.onReceive(bid2);
    orderBook.onReceive(bid3);
    orderBook.onReceive(bid4);
    orderBook.onReceive(bid5);
    orderBook.onReceive(bid6);
    orderBook.onReceive(bid7);
    orderBook.onReceive(bid8);
    orderBook.onReceive(bid9);
    orderBook.onReceive(ask1);

    assertThat(orderBook).isNotNull();
  }

  @Test
  public void addLimitTest8() throws Exception {
    // processing 2015-05-21T16:21:10.190-04:00,1,67,6.4096193,BID,hft2
    // processing 2015-05-21T16:21:10.190-04:00,2,55,7.4102798,BID,hft1
    // processing 2015-05-21T16:21:10.329-04:00,3,66,10.0,BID,hft1
    // processing 2015-05-21T16:21:10.329-04:00,4,77,7.4102798,ASK,hft2

    val bid1 = new Order(BID, LIMIT_ADD, time, 1, brokerId, symbol, 67, 6.4096193f, "u1");
    val bid2 = new Order(BID, LIMIT_ADD, time, 2, brokerId, symbol, 55, 7.4102798f, "u1");
    val bid3 = new Order(BID, LIMIT_ADD, time.plusMinutes(1), 3, brokerId, symbol, 66, 10.0f, "u1");
    val ask1 = new Order(ASK, LIMIT_ADD, time.plusMinutes(1), 4, brokerId, symbol, 77, 7.4102798f, "u1");

    orderBook.onReceive(bid1);
    orderBook.onReceive(bid2);
    orderBook.onReceive(bid3);
    orderBook.onReceive(ask1);

    orderBook.printBook();
  }

  public void addLimitTest9() {
    // val order1 = new Order(ASK, ADD, time, 1,
    // brokerId, symbol, 1000, 25f,"u1");
    // val order2 = new Order(ASK, ADD,
    // time.plusMinutes(1), 2, brokerId, symbol, 1000, 25f,"u2");
    // val order6 = new Order(ASK, ADD,
    // time.plusMinutes(4), 5, brokerId, symbol, 10000, 19f,"u3");
    // val order7 = new Order(ASK, ADD,
    // time.plusMinutes(5), 6, brokerId, symbol, 1000, 26f,"u4");
    //
    // val order3 = new Order(BID, ADD,
    // time.plusMinutes(2), 3, brokerId, symbol, 1000, 20f,"u5");
    // val order4 = new Order(BID, ADD,
    // time.plusMinutes(3), 4, brokerId, symbol, 5000, 19f,"u6");
    // val order5 = new Order(BID, ADD,
    // time.plusMinutes(6), 7, brokerId, symbol, 5000, 19f,"u7");
  }

}

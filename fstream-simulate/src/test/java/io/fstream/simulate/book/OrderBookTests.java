package io.fstream.simulate.book;

import static org.junit.Assert.assertTrue;
import io.fstream.simulate.agent.Exchange;
import io.fstream.simulate.orders.LimitOrder;
import io.fstream.simulate.orders.Order.OrderSide;
import io.fstream.simulate.orders.Order.OrderType;
import io.fstream.simulate.publisher.LogPublisher;
import lombok.val;

import org.joda.time.DateTime;
import org.junit.Test;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;

public class OrderBookTests {

  @Test
  public void testAddLimitOrders() {
    val ob = createOrderBook();
    val now = DateTime.now();

    val ask1 = new LimitOrder(OrderSide.ASK, OrderType.ADD, now, 1, "tsx",
        "RY", 1000, 25f, "u1");
    val ask2 = new LimitOrder(OrderSide.ASK, OrderType.ADD,
        now.plusMinutes(1), 2, "tsx", "RY", 1000, 26f, "u2");
    val ask3 = new LimitOrder(OrderSide.ASK, OrderType.ADD,
        now.plusMinutes(2), 3, "tsx", "RY", 1000, 24f, "u3");
    val ask4 = new LimitOrder(OrderSide.ASK, OrderType.ADD,
        now.plusMinutes(3), 4, "tsx", "RY", 1000, 24f, "u4");

    val bid5 = new LimitOrder(OrderSide.ASK, OrderType.ADD, now.plus(4), 4,
        "tsx", "RY", 1000, 19f, "u5");
    val bid6 = new LimitOrder(OrderSide.ASK, OrderType.ADD,
        now.plusMinutes(5), 5, "tsx", "RY", 1000, 19f, "u6");
    val bid7 = new LimitOrder(OrderSide.ASK, OrderType.ADD,
        now.plusMinutes(6), 6, "tsx", "RY", 1000, 18f, "u7");

    ob.processOrder(ask1);
    assertTrue(ob.getAsks().size() == 1);
    assertTrue(ob.getAskdepth() == 1000);
    assertTrue(ob.getBestask() == 25f);

    ob.processOrder(ask2);
    assertTrue(ob.getAsks().size() == 2);
    assertTrue(ob.getAskdepth() == 2000);
    assertTrue(ob.getBestask() == 25f);

    ob.processOrder(ask3);
    assertTrue(ob.getAsks().size() == 3);
    assertTrue(ob.getAskdepth() == 3000);
    assertTrue(ob.getBestask() == 24f);

    ob.processOrder(ask4);
    assertTrue(ob.getAsks().size() == 3);
    assertTrue(ob.getAskdepth() == 4000);
    assertTrue(ob.getBestask() == 24f);
    assertTrue(ob.getAsks().get(24f).size() == 2);
    LimitOrder[] orderlist = ob.getAsks().get(24f)
        .toArray(new LimitOrder[0]);
    assertTrue(orderlist.length == 2);
    assertTrue(orderlist[0].getSentTime().getMillis() <= orderlist[1]
        .getSentTime().getMillis());

    ob.processOrder(bid5);
    ob.processOrder(bid6);
    ob.processOrder(bid7);
  }

  @Test
  public void addLimitTest2() {
    val ob = createOrderBook();
    val now = DateTime.now();

    val ask0 = new LimitOrder(OrderSide.ASK, OrderType.ADD, now, 1, "tsx",
        "RY", 91, 10.277141f, "u1");
    val bid1 = new LimitOrder(OrderSide.BID, OrderType.ADD,
        now.plusMinutes(1), 1, "tsx", "RY", 36, 6.828021f, "u1");
    val bid2 = new LimitOrder(OrderSide.BID, OrderType.ADD,
        now.plusMinutes(2), 1, "tsx", "RY", 63, 6.5065575f, "u1");
    val bid3 = new LimitOrder(OrderSide.BID, OrderType.ADD,
        now.plusMinutes(3), 1, "tsx", "RY", 51, 3.4671168f, "u1");
    val bid4 = new LimitOrder(OrderSide.BID, OrderType.ADD,
        now.plusMinutes(4), 1, "tsx", "RY", 35, 4.0023165f, "u1");
    val bid5 = new LimitOrder(OrderSide.BID, OrderType.ADD,
        now.plusMinutes(5), 1, "tsx", "RY", 47, 5.373663f, "u1");
    val bid6 = new LimitOrder(OrderSide.BID, OrderType.ADD,
        now.plusMinutes(6), 1, "tsx", "RY", 52, 10.277141f, "u1");

    ob.processOrder(ask0);
    ob.processOrder(bid1);
    ob.processOrder(bid2);
    ob.processOrder(bid3);
    ob.processOrder(bid4);
    ob.processOrder(bid5);
    ob.processOrder(bid6);

    // ob.printBook();

  }

  @Test
  public void addLimitTest3() {
    val ob = createOrderBook();
    val now = DateTime.now();

    // processing 2015-05-20T10:29:05.506-04:00,34,7.9161587,BID,hft1
    // processing 2015-05-20T10:29:05.506-04:00,89,10.97681,ASK,hft2
    // processing 2015-05-20T10:29:05.647-04:00,90,7.9161587,ASK,hft1
    // Trade registered for active ASK of 34 at price 7.9161587
    // processing 2015-05-20T10:29:05.647-04:00,84,7.9161587,ASK,hft2
    // ask depth does not add up record = 229 actual =145

    val bid0 = new LimitOrder(OrderSide.BID, OrderType.ADD, now, 0, "tsx",
        "RY", 34, 7.9161587f, "u1");
    val bid1 = new LimitOrder(OrderSide.BID, OrderType.ADD, now, 1, "tsx",
        "RY", 50, 7.9161587f, "u1");
    val ask1 = new LimitOrder(OrderSide.ASK, OrderType.ADD,
        now.plusMinutes(1), 2, "tsx", "RY", 89, 10.97681f, "u1");
    val ask2 = new LimitOrder(OrderSide.ASK, OrderType.ADD,
        now.plusMinutes(2), 3, "tsx", "RY", 90, 7.9161587f, "u1");
    val ask3 = new LimitOrder(OrderSide.ASK, OrderType.ADD,
        now.plusMinutes(3), 4, "tsx", "RY", 84, 7.9161587f, "u1");

    ob.processOrder(bid0);
    ob.processOrder(bid1);
    ob.processOrder(ask1);
    ob.processOrder(ask2);
    ob.processOrder(ask3);

    ob.printBook();

  }

  @Test
  public void addLimitTest4() {
    // processing 2015-05-20T21:02:27.593-04:00,7,6.180016,BID,hft2
    // processing 2015-05-20T21:02:27.593-04:00,9,10.0,BID,hft1
    // processing 2015-05-20T21:02:27.740-04:00,55,10.0,BID,hft2
    // processing 2015-05-20T21:02:27.741-04:00,83,10.0,ASK,hft1

    val ob = createOrderBook();
    val now = DateTime.now();

    val bid1 = new LimitOrder(OrderSide.BID, OrderType.ADD, now, 1, "tsx",
        "RY", 7, 6.180016f, "u1");
    val bid2 = new LimitOrder(OrderSide.BID, OrderType.ADD, now, 2, "tsx",
        "RY", 9, 10.0f, "u1");
    val bid3 = new LimitOrder(OrderSide.BID, OrderType.ADD,
        now.plusMinutes(1), 3, "tsx", "RY", 55, 10.0f, "u1");
    val ask1 = new LimitOrder(OrderSide.ASK, OrderType.ADD,
        now.plusMinutes(2), 4, "tsx", "RY", 83, 10.0f, "u1");

    ob.processOrder(bid1);
    ob.processOrder(bid2);
    ob.processOrder(bid3);
    ob.processOrder(ask1);
    ob.printBook();
  }

  @Test
  public void addLimitTest5() {
    // processing 2015-05-20T22:19:11.145-04:00,1,47,7.5659113,BID,hft2
    // processing 2015-05-20T22:19:11.145-04:00,2,48,10.0,BID,hft1
    // processing 2015-05-20T22:19:11.284-04:00,3,35,10.0,BID,hft1
    // processing 2015-05-20T22:19:11.284-04:00,4,32,10.0,BID,hft2

    val ob = createOrderBook();
    val now = DateTime.now();

    val bid1 = new LimitOrder(OrderSide.BID, OrderType.ADD, now, 1, "tsx",
        "RY", 47, 7.5659113f, "u1");
    val bid2 = new LimitOrder(OrderSide.BID, OrderType.ADD, now, 2, "tsx",
        "RY", 48, 10.0f, "u1");
    val bid3 = new LimitOrder(OrderSide.BID, OrderType.ADD,
        now.plusMinutes(1), 3, "tsx", "RY", 35, 10.0f, "u1");
    val bid4 = new LimitOrder(OrderSide.BID, OrderType.ADD,
        now.plusMinutes(1), 4, "tsx", "RY", 32, 10.0f, "u1");

    ob.processOrder(bid1);
    ob.processOrder(bid2);
    ob.processOrder(bid3);
    ob.processOrder(bid4);
    ob.printBook();
  }

  @Test
  public void addLimitTest6() {
    // processing 2015-05-21T12:48:20.437-04:00,1,81,8.0,ASK,hft2
    // processing 2015-05-21T12:48:20.437-04:00,2,91,11.686356,ASK,hft1
    // processing 2015-05-21T12:48:20.574-04:00,3,98,8.0,ASK,hft2
    // processing 2015-05-21T12:48:20.574-04:00,4,32,8.0,BID,hft1

    val ob = createOrderBook();
    val now = DateTime.now();

    val ask1 = new LimitOrder(OrderSide.ASK, OrderType.ADD, now, 1, "tsx",
        "RY", 81, 8.0f, "u1");
    val ask2 = new LimitOrder(OrderSide.ASK, OrderType.ADD, now, 2, "tsx",
        "RY", 91, 11.686356f, "u1");
    val ask3 = new LimitOrder(OrderSide.ASK, OrderType.ADD,
        now.plusMinutes(1), 3, "tsx", "RY", 98, 8.0f, "u1");
    val bid1 = new LimitOrder(OrderSide.BID, OrderType.ADD,
        now.plusMinutes(1), 4, "tsx", "RY", 32, 8.0f, "u1");
    ob.processOrder(ask1);
    ob.processOrder(ask2);
    ob.processOrder(ask3);
    ob.processOrder(bid1);
    ob.printBook();

  }

  @Test
  public void addLimitTest7() {
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
    ActorSystem testTradingApp = ActorSystem.create("testTradingApp");
    final Props props = Props.create(Exchange.class);
    final TestActorRef<Exchange> refexchange = TestActorRef.create(
        testTradingApp, props, "testExchange");

    final Props props2 = Props.create(OrderBook.class, "RY", refexchange);
    final TestActorRef<OrderBook> refob = TestActorRef.create(
        testTradingApp, props2, "testOB");
    final OrderBook ob = refob.underlyingActor();
    DateTime now = DateTime.now();

    val bid1 = new LimitOrder(OrderSide.BID, OrderType.ADD, now, 1, "tsx",
        "RY", 50, 7.8310976f, "u1");
    val bid2 = new LimitOrder(OrderSide.BID, OrderType.ADD, now, 2, "tsx",
        "RY", 67, 6.1404834f, "u1");
    val bid3 = new LimitOrder(OrderSide.BID, OrderType.ADD,
        now.plusMinutes(1), 3, "tsx", "RY", 63, 10.0f, "u1");
    val bid4 = new LimitOrder(OrderSide.BID, OrderType.ADD,
        now.plusMinutes(1), 4, "tsx", "RY", 9, 6.307989f, "u1");
    val bid5 = new LimitOrder(OrderSide.BID, OrderType.ADD,
        now.plusMinutes(2), 5, "tsx", "RY", 76, 10.0f, "u1");
    val bid6 = new LimitOrder(OrderSide.BID, OrderType.ADD,
        now.plusMinutes(2), 6, "tsx", "RY", 15, 10.0f, "u1");
    val bid7 = new LimitOrder(OrderSide.BID, OrderType.ADD,
        now.plusMinutes(3), 7, "tsx", "RY", 2, 10.0f, "u1");
    val bid8 = new LimitOrder(OrderSide.BID, OrderType.ADD,
        now.plusMinutes(3), 8, "tsx", "RY", 32, 10.0f, "u1");
    val bid9 = new LimitOrder(OrderSide.BID, OrderType.ADD,
        now.plusMinutes(4), 9, "tsx", "RY", 12, 10.0f, "u1");
    val ask1 = new LimitOrder(OrderSide.ASK, OrderType.ADD,
        now.plusMinutes(4), 11, "tsx", "RY", 95, 10.0f, "u1");

    ob.processOrder(bid1);
    ob.processOrder(bid2);
    ob.processOrder(bid3);
    ob.processOrder(bid4);
    ob.processOrder(bid5);
    ob.processOrder(bid6);
    ob.processOrder(bid7);
    ob.processOrder(bid8);
    ob.processOrder(bid9);
    ob.processOrder(ask1);
  }

  @Test
  public void addLimitTest8() {
    // processing 2015-05-21T16:21:10.190-04:00,1,67,6.4096193,BID,hft2
    // processing 2015-05-21T16:21:10.190-04:00,2,55,7.4102798,BID,hft1
    // processing 2015-05-21T16:21:10.329-04:00,3,66,10.0,BID,hft1
    // processing 2015-05-21T16:21:10.329-04:00,4,77,7.4102798,ASK,hft2

    ActorSystem testTradingApp = ActorSystem.create("testTradingApp");
    final Props props = Props.create(Exchange.class);
    final TestActorRef<Exchange> refexchange = TestActorRef.create(
        testTradingApp, props, "testExchange");

    final Props props2 = Props.create(OrderBook.class, "RY", refexchange);
    final TestActorRef<OrderBook> refob = TestActorRef.create(
        testTradingApp, props2, "testOB");
    final OrderBook ob = refob.underlyingActor();
    DateTime now = DateTime.now();

    val bid1 = new LimitOrder(OrderSide.BID, OrderType.ADD, now, 1, "tsx",
        "RY", 67, 6.4096193f, "u1");
    val bid2 = new LimitOrder(OrderSide.BID, OrderType.ADD, now, 2, "tsx",
        "RY", 55, 7.4102798f, "u1");
    val bid3 = new LimitOrder(OrderSide.BID, OrderType.ADD,
        now.plusMinutes(1), 3, "tsx", "RY", 66, 10.0f, "u1");
    val ask1 = new LimitOrder(OrderSide.ASK, OrderType.ADD,
        now.plusMinutes(1), 4, "tsx", "RY", 77, 7.4102798f, "u1");

    ob.processOrder(bid1);
    ob.processOrder(bid2);
    ob.processOrder(bid3);
    ob.processOrder(ask1);
    ob.printBook();
  }

  public void addLimitTest9() {
    // val order1 = new LimitOrder(OrderSide.ASK, OrderType.ADD, now, 1,
    // "tsx", "RY", 1000, 25f,"u1");
    // val order2 = new LimitOrder(OrderSide.ASK, OrderType.ADD,
    // now.plusMinutes(1), 2, "tsx", "RY", 1000, 25f,"u2");
    // val order6 = new LimitOrder(OrderSide.ASK, OrderType.ADD,
    // now.plusMinutes(4), 5, "tsx", "RY", 10000, 19f,"u3");
    // val order7 = new LimitOrder(OrderSide.ASK, OrderType.ADD,
    // now.plusMinutes(5), 6, "tsx", "RY", 1000, 26f,"u4");
    //
    // val order3 = new LimitOrder(OrderSide.BID, OrderType.ADD,
    // now.plusMinutes(2), 3, "tsx", "RY", 1000, 20f,"u5");
    // val order4 = new LimitOrder(OrderSide.BID, OrderType.ADD,
    // now.plusMinutes(3), 4, "tsx", "RY", 5000, 19f,"u6");
    // val order5 = new LimitOrder(OrderSide.BID, OrderType.ADD,
    // now.plusMinutes(6), 7, "tsx", "RY", 5000, 19f,"u7");
    //
  }

  private OrderBook createOrderBook() {
    ActorSystem testTradingApp = ActorSystem.create("testTradingApp");
    final Props props = Props.create(Exchange.class);
    final TestActorRef<Exchange> refexchange = TestActorRef.create(
        testTradingApp, props, "testExchange");

    final Props props2 = Props.create(OrderBook.class, "RY", refexchange);
    final TestActorRef<OrderBook> refob = TestActorRef.create(
        testTradingApp, props2, "testOB");
    final OrderBook ob = refob.underlyingActor();
    ob.setPublisher(new LogPublisher());
    return ob;
  }

}

package io.fstream.simulate.agents;

import io.fstream.simulate.messages.ActiveInstruments;
import io.fstream.simulate.messages.BbBo;
import io.fstream.simulate.messages.Messages;
import io.fstream.simulate.messages.State;
import io.fstream.simulate.orders.IOrder;
import io.fstream.simulate.orders.IOrder.OrderSide;
import io.fstream.simulate.orders.IOrder.OrderType;
import io.fstream.simulate.orders.LimitOrder;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import akka.util.Timeout;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;



@Getter
@Setter
@Slf4j
public class RetailAgent extends AgentActor { 
	
	/**
	 * data structures    s
	 */
	HashMap<String,Integer> positions;
	ActiveInstruments activeinstruments = new ActiveInstruments();
	final float PROB_MARKET = 0.49f;
	final float PROB_BUY = 0.49f;
	final float PROB_BESTPRICE = 0.19f;
	
	public RetailAgent (String name,ActorRef exchange) {
		super(name,exchange);
	}
		
	public void executeAction() {
		IOrder order = createOrder();
		if (order != null) {
			exchange.tell(order, self());
		}		
	}
	
	private IOrder createOrder ()  {
		int next = random.nextInt(max_trade_size);
		int amount = next+1;
		OrderSide side;
		OrderType type = OrderType.ADD;
		float price;
		
		if (activeinstruments.getActiveinstruments() == null) {
			// send a message to exchange and then return null and wait for next decision iteration
			exchange.tell(activeinstruments, self());
			return null;
		}
		String symbol = activeinstruments.getActiveinstruments().get(random.nextInt(activeinstruments.getActiveinstruments().size()));
		
		BbBo bbbo = new BbBo(symbol);
		Future<Object> futurestate = Patterns.ask(exchange, bbbo, bookquerytimeout);
		
		try {
			bbbo = (BbBo) Await.result(futurestate, bookquerytimeout.duration());
		} catch (Exception e) {
			log.error("timeout awaiting state");
			return null;
		}
		
		
		
		float bestbid = bbbo.getBestbid() != Float.MIN_VALUE ? bbbo.getBestbid() : 8;
		float bestask = bbbo.getBestoffer() != Float.MAX_VALUE ? bbbo.getBestoffer() : 10;
		float mid = (bestask+bestbid)/2;
		
		side = decideSide(1-PROB_BUY, OrderSide.ASK);
		
		type = decideOrderType(PROB_MARKET);
		
		if (type == OrderType.MO) {
			if (side == OrderSide.ASK) {
				price = Float.MIN_VALUE;
			}
			else {
				price = Float.MAX_VALUE;
			}
		}
		else {
			if (side == OrderSide.ASK) {
				price = decidePrice(bestask, bestask+5, bestask, PROB_BESTPRICE);
			}
			else {
				price = decidePrice(bestbid-5, bestbid, bestask, PROB_BESTPRICE);
			}
			
		}
		
		return new LimitOrder(side, type, DateTime.now(), Exchange.getOID(), "xx", symbol, amount, price, name);
	}



	@Override
	public void onReceive(Object message) throws Exception {
		log.debug("agent message received by " + this.getName() + " " + message.toString());
		if (message instanceof State) {
			State state = (State)message;
		}
		else if (message instanceof String) {
			if (((String)message).equals(Messages.AGENT_EXECUTE_ACTION)) {
				this.executeAction();
				getContext().system().scheduler().scheduleOnce(Duration.create(sleep, TimeUnit.SECONDS), getSelf(), Messages.AGENT_EXECUTE_ACTION, getContext().dispatcher(), null);
			}	
		}
		else if (message instanceof ActiveInstruments) {
			this.activeinstruments.setActiveinstruments(((ActiveInstruments)message).getActiveinstruments());
		}
		else {
			unhandled(message);
		}
		
	}
	
	@Override 
	public void preStart() {
		getContext().system().scheduler().scheduleOnce(Duration.create(sleep, TimeUnit.SECONDS), getSelf(), Messages.AGENT_EXECUTE_ACTION, getContext().dispatcher(), null);
	}
	
	@Override
	public void postRestart(Throwable reason) {
		
	}

	
}

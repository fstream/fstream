package io.fstream.simulate.agents;

import io.fstream.simulate.messages.ActiveInstruments;
import io.fstream.simulate.messages.BbBo;
import io.fstream.simulate.messages.Messages;
import io.fstream.simulate.messages.State;
import io.fstream.simulate.orders.IOrder;
import io.fstream.simulate.orders.IOrder.OrderSide;
import io.fstream.simulate.orders.IOrder.OrderType;
import io.fstream.simulate.orders.LimitOrder;
import io.fstream.simulate.orders.Positions;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import akka.util.Timeout;

@Slf4j
@Getter
public class InstitutionalAgent extends UntypedActor implements IAgent {
	
	Random random;
	private int sleep;
	private String name;
	private ActorRef exchange;
	final int MAX_TRADE_SIZE = 5000;
	ActiveInstruments activeinstruments = new ActiveInstruments();
	Timeout bookquerytimeout = new Timeout(Duration.create(5, "seconds"));
	Positions positions;

	public InstitutionalAgent (String name,ActorRef exchange) {
		
		this.name = name;
		this.exchange = exchange;
		init();
	}
	
	private void init () {
		random = new Random();
		this.sleep = random.nextInt(5)+1;
		positions = new Positions();
	}
	@Override
	public void executeAction() {
		IOrder order = createOrder();
		if (order != null) {
			exchange.tell(order, self());
		}
	}
	
	private IOrder createOrder () {
		int next = random.nextInt(MAX_TRADE_SIZE);
		int amount = next+1;
		OrderType type = OrderType.ADD;
		OrderSide side;
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
		
		if (random.nextDouble() > 0.49 ) {
			side = OrderSide.ASK;
			if (random.nextDouble() > 0.40) {
				price = Math.min(bestask + random.nextFloat(),15);
			} else {
				price = Float.MIN_VALUE; // trigger market order
				type = OrderType.MO;
			}
			
		}
		else {
			side = OrderSide.BID;
			if (random.nextDouble() > 0.40) {
				price = Math.max(bestbid - random.nextFloat(),5);
			} else {
				//price = bestask; // trigger market order
				price = Float.MAX_VALUE;
				type = OrderType.MO;
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

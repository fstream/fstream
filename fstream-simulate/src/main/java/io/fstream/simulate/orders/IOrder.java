package io.fstream.simulate.orders;

import org.joda.time.DateTime;

//TODO bring back MarketOrder. LimitOrder (has price) extend MarketOrder ?

public interface IOrder {
	
	public enum OrderSide {BID,ASK}; // ASK(OFFER)/BID
	public enum OrderType {MO, ADD, AMEND, CANCEL} // Market Order, Limit Order (add, amend, cancel)
	
	public OrderType getType();
	
	public OrderSide getSide();
	
	public DateTime getSentTime();
	
	public int getOid();
	
	public String getBrokerId();
	
	public int getAmount();
	
	public String getSymbol();
	
	public float getPrice();
	
	public String getUserId();
	
	public DateTime getReceivedTime();

}

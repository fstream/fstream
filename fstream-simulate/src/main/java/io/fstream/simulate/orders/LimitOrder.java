package io.fstream.simulate.orders;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

import org.joda.time.DateTime;

@Getter
@Setter
public class LimitOrder implements IOrder, Comparable<LimitOrder> {
	OrderSide side;
	OrderType type;
	DateTime sentTime;
	int oid;
	String brokerId;
	String symbol;
	int amount;
	float price;
	String userid;
	DateTime receivedTime;
	

	public LimitOrder (OrderSide side, OrderType type,DateTime time, int oid,String brokerId,String symbol, int amount, float price,String userid) {
		this.side = side;
		this.type = type;
		this.sentTime = time;
		this.oid = oid;
		this.brokerId = brokerId;
		this.symbol = symbol;
		this.amount = amount;
		this.price = price;
		this.userid = userid;
		
	}
	
	@Override
	public boolean equals (Object obj) {
		val order = (LimitOrder)obj;
		if (order.getBrokerId() == this.brokerId && order.getOid() == this.oid && order.getSentTime().equals(this.getSentTime())) {
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.getBrokerId().hashCode() + this.getOid() + this.getSentTime().hashCode();
	}
	@Override
	public int compareTo(LimitOrder o) {
		if (this.getOid() == o.getOid() && this.brokerId == o.getBrokerId() ) {
			return 1;
		}
		return 0;
	}

	@Override
	public String getUserId() {
		return this.userid;
	}
	
	@Override
	public String toString() {
		return String.format("%s,%s,%s,%s,%s,%s,%s", this.getSentTime(),this.getOid(),this.getSymbol(),this.getAmount(),this.getPrice(),this.getSide(),this.getUserId());
		
	}

	@Override
	public DateTime getSentTime() {
		return sentTime;
	}

}

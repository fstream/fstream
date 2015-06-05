package io.fstream.simulate.orders;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.joda.time.DateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Quote {
	
	float askprice;
	float bidprice;
	DateTime time;
	String symbol;
	
	
	public Quote (DateTime time, String symbol,float ask, float bid) {
		this.time = time;
		this.symbol = symbol;
		this.askprice = ask;
		this.bidprice = bid;
	}

}

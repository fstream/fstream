package io.fstream.simulate.orders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

/**
 * Class providing accounting services to trading agents for position tracking
 * 
 * @author bdevani
 *
 */
@Getter
@Setter
public class Positions {

  private HashMap<String, List<Order>> positions;

  public Positions() {
    positions = new HashMap<String, List<Order>>();
  }

  public List<Order> getPositions(String symbol) {
    return positions.get(symbol);
  }

  public int getPositionSize(String symbol) {
    int size = 0;
    val symbolpos = positions.get(symbol);
    if (symbolpos == null || symbolpos.isEmpty()) {
      return size;
    }
    for (val order : symbolpos) {
      size = size + order.getAmount();
    }
    return size;
  }

  public boolean addPosition(Order order) {
    if (positions.get(order.getSymbol()) != null) {
      return positions.get(order.getSymbol()).add(order);
    } else {
      val orderlist = new ArrayList<Order>();
      orderlist.add(order);
      positions.put(order.getSymbol(), orderlist);
      return true;
    }
  }

}

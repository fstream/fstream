/*
 * Copyright (c) 2014 The Ontario Institute for Cancer Research. All rights reserved.                             
 *                                                                                                               
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with                                  
 * this program. If not, see <http://www.gnu.org/licenses/>.                                                     
 *                                                                                                               
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY                           
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES                          
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT                           
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,                                
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED                          
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;                               
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER                              
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN                         
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.fstream.core.fix;

import static io.fstream.core.fix.OandaFixApplication.RATES_SUB_ID;
import static quickfix.field.MDEntryType.BID;
import static quickfix.field.MDEntryType.OFFER;
import lombok.val;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.MDUpdateAction;
import quickfix.field.MDUpdateType;
import quickfix.field.MarketDepth;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.field.TargetSubID;
import quickfix.fix44.MarketDataIncrementalRefresh;
import quickfix.fix44.MarketDataRequest;
import quickfix.fix44.MarketDataSnapshotFullRefresh;
import quickfix.fix44.Message;

public class Messages {

  public static Message message1() {
    val symbol = new Symbol("EUR/USD");

    val message = new MarketDataIncrementalRefresh();

    val header = message.getHeader();
    header.setField(new TargetSubID(RATES_SUB_ID));
    header.setField(symbol);
    header.setField(new MDUpdateAction(MDUpdateAction.CHANGE));

    val group = new MarketDataIncrementalRefresh.NoMDEntries();
    group.set(new MDEntryType(BID));
    message.addGroup(group);
    group.set(new MDEntryType(OFFER));
    message.addGroup(group);

    return message;
  }

  public static Message message2() {
    val symbol = new Symbol("EUR/USD");

    val message = new MarketDataSnapshotFullRefresh();

    val header = message.getHeader();
    header.setField(new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES));
    header.setField(new TargetSubID(RATES_SUB_ID));
    header.setField(symbol);

    val group = new MarketDataSnapshotFullRefresh.NoMDEntries();
    group.set(new MDEntryType(BID));
    message.addGroup(group);
    group.set(new MDEntryType(OFFER));
    message.addGroup(group);

    return message;
  }

  public static Message message3() {
    val symbol = new Symbol("EUR/USD");

    val message = new MarketDataSnapshotFullRefresh();

    val header = message.getHeader();
    header.setField(new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT));
    header.setField(new TargetSubID(RATES_SUB_ID));
    header.setField(symbol);

    val group = new MarketDataSnapshotFullRefresh.NoMDEntries();
    group.set(new MDEntryType(BID));
    message.addGroup(group);
    group.set(new MDEntryType(OFFER));
    message.addGroup(group);

    return message;
  }

  public static Message message4() {
    val symbol = new Symbol("EUR/USD");
    MarketDataRequest marketDataRequest = new MarketDataRequest();
    marketDataRequest.set(new MDReqID("1"));
    marketDataRequest.set(new MarketDepth(1));
    marketDataRequest.set(new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES));
    marketDataRequest.set(new MDUpdateType(MDUpdateType.FULL_REFRESH));
    MarketDataRequest.NoMDEntryTypes et = new MarketDataRequest.NoMDEntryTypes();
    et.set(new MDEntryType(MDEntryType.BID));
    marketDataRequest.addGroup(et);
    et.set(new MDEntryType(MDEntryType.OFFER));
    marketDataRequest.addGroup(et);
    MarketDataRequest.NoRelatedSym rs = new MarketDataRequest.NoRelatedSym();
    rs.set(symbol);
    marketDataRequest.addGroup(rs);

    marketDataRequest.getHeader().setField(new TargetSubID(RATES_SUB_ID));

    return marketDataRequest;
  }

}

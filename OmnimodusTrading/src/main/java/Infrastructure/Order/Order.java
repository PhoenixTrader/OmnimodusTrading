package Order;

import Quote.Quote;
import java.io.*;
import java.util.*;
import QuoteDataFeed.QuoteDataFeed;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Order implements Cloneable{
	private String assetClass;
	private String action; // OPEN or CLOSE
	private String type;// BUY or SELL
	private String status; // PENING or FILLED
	private double volume;
	private String underlying;// Just the ticker

	private double limit;
	private double takeProfit;
	private double stopLoss;

	private Quote openReferenceQuote;
	private Quote openFillQuote;

	private Quote closeReferenceQuote;
	private Quote closeFillQuote;

	private String comment;

	private int identifier1; // first identifier for differentiating between strategy parts
	private int identifier2;

	// ----- Get functions
	public String GetAction() {
		return this.action;
	}

	public String GetStatus() {
		return this.status;
	}

	public String GetType() {
		return this.type;
	}

	public double GetVolume() {
		return this.volume;
	}

	public String GetUnderlying() {
		return this.underlying;
	}// Just the ticker

	public double GetLimit() {
		return this.limit;
	}

	public double GetTakeProfit() {
		return this.takeProfit;
	}

	public double GetStopLoss() {
		return this.stopLoss;
	}

	public Quote GetOpenReferenceQuote() {
		return this.openReferenceQuote;
	}

	public double GetOpenReferencePrice() {
		return this.type == "BUY" ? this.openReferenceQuote.GetAsk() : this.openReferenceQuote.GetBid();
	}

	public double GetOpenFillPrice() {
		return this.type == "BUY" ? this.openFillQuote.GetAsk() : this.openFillQuote.GetBid();
	}

	public double GetCloseFillPrice() {
		return this.type == "BUY" ? this.closeFillQuote.GetBid() : this.closeFillQuote.GetAsk();
	}

	public double GetPnL() {
		return 0.0;
	}

	public ZonedDateTime GetOpenFillTime()
	{
	return this.openFillQuote.GetTime();}

	public Quote GetOpenFillQuote() {
		return this.openFillQuote;
	}

	public Quote GetCloseReferenceQuote() {
		return this.closeReferenceQuote;
	}

	public double GetCloseReferencePrice() {
		return this.type == "BUY" ? this.closeReferenceQuote.GetBid() : this.closeReferenceQuote.GetAsk();
	}

	public Quote GetCloseFillQuote() {
		return this.closeFillQuote;
	}

	public String GetComment() {
		return this.comment;
	}

	public int GetIdentifier1() {
		return this.identifier1;
	} // first identifier for differentiating between strategy parts

	public int GetIdentifier2() {
		return this.identifier2;
	}

	public String GetAssetClass() {
		return this.assetClass;
	}

	public Order(String action, String buySell, double size, String assetClass, String underlying,
			QuoteDataFeed quoteDataFeed, double limit, double takeProfit, double stopLoss, int identifier1,
			int identifier2, String comment) {

		this.assetClass = assetClass;
		this.action = action;// # OPEN or CLOSE
		this.type = buySell;// # BUY or SELL
		this.status = "PENDING";// #PENING or FILLED
		this.volume = size;
		this.underlying = underlying;// # Just the ticker

		this.limit = limit;
		this.takeProfit = takeProfit;
		this.stopLoss = stopLoss;

		this.openReferenceQuote = quoteDataFeed.GetQuote(assetClass, underlying);
		this.openFillQuote = null;

		this.closeReferenceQuote = null;

		this.closeFillQuote = null;

		this.comment = comment;

		this.identifier1 = identifier1;// # first identifier for differentiating between strategy parts
		this.identifier2 = identifier2;// # second identifier for differentiating between single orders

	};

	public Order(String action, String buySell, double size, String assetClass, String underlying,
			QuoteDataFeed quoteDataFeed, int identifier1, int identifier2, String comment) {
		this.assetClass = assetClass;
		this.action = action;// # OPEN or CLOSE
		this.type = buySell;// # BUY or SELL
		this.status = "PENDING";// #PENING or FILLED
		this.volume = size;
		this.underlying = underlying;// # Just the ticker

		this.openReferenceQuote = quoteDataFeed.GetQuote(assetClass, underlying);
		this.openFillQuote = null;

		this.closeReferenceQuote = null;

		this.closeFillQuote = null;

		this.comment = comment;

		this.identifier1 = identifier1;// # first identifier for differentiating between strategy parts
		this.identifier2 = identifier2;// # second identifier for differentiating between single orders

	};

	public Order() {
	};

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public Order Close(QuoteDataFeed quoteDataFeed, String comment) {
		this.action = "CLOSE";
		this.comment = comment;
		this.closeReferenceQuote = quoteDataFeed.GetQuote(this.assetClass, this.underlying);
		return this;
	}

	public static Order Close(Order order, QuoteDataFeed quoteDataFeed, String comment) {
		try {
			return ((Order) order.clone()).Close(quoteDataFeed, comment);
		} catch (CloneNotSupportedException c) {
			c.printStackTrace();
			return null;//order.Close(quoteDataFeed, comment);
		}
	}

	public static int compareOpenPrice(Order o1, Order o2) {
		return Double.compare(o1.GetOpenReferencePrice(), o2.GetOpenReferencePrice());
	}

	public void FillStatic() {
		this.status = "FILLED";
		if (this.action == "OPEN") {
			this.openFillQuote = this.openReferenceQuote;
		}
		if (this.action == "CLOSE") {
			this.closeFillQuote = this.openFillQuote;
		}
	}


	public double PnL(Quote quote) {
		if (this.type == "BUY")
			return this.volume * (quote.GetBid() - this.openFillQuote.GetAsk());
		if (this.type == "SELL")
			return this.volume * (this.openFillQuote.GetBid() - quote.GetAsk());

		return -99999999999999999999.9;
	}

	public double GetClosePnLFX() {
		if (this.type == "BUY") {
			return this.volume * (this.closeFillQuote.GetBid() - this.openFillQuote.GetAsk())*100000;
		}
		if (this.type == "SELL")
			return this.volume * (this.openFillQuote.GetBid() - this.closeFillQuote.GetAsk())*100000;

		return -99999999999999999999.9;
	}

	public static ArrayList<Order> RemoveOrderFromList(ArrayList<Order> orderList, Order orderToRemove) {

		for (int i = 0; i < orderList.size(); i++) {
			if (orderList.get(i).GetIdentifier1() == orderToRemove.GetIdentifier1())
				orderList.remove(i);
		}

		return orderList;
	}

	public static int GetIndex(ArrayList<Order> orderList, Order orderToRemove) {
		try {
			for (int i = 0; i < orderList.size(); i++) {
				if (orderList.get(i).GetIdentifier1() == orderToRemove.GetIdentifier1())
					return i;
			}
		} catch (Exception e) {
			System.out.print("Order not in List\n");
			return -1;
		}
		return -1;
	}
	
	public double GetFloatingPnL(QuoteDataFeed quoteDataFeed)
	{

		return this.PnL(quoteDataFeed.GetQuote(this.assetClass, this.underlying));
	}

	public void Print()
	{
		System.out.print(this.GetOpenFillTime().format(DateTimeFormatter.ISO_DATE_TIME) + " " +this.action + " " + this.type +" " + Double.toString(this.GetVolume()) + " lots " + this.GetUnderlying() + "@" + Double.toString(this.GetOpenFillPrice()) +"\n");
	}

	/*
	 * Comparator for sorting the list by roll no public class NewClass2 implements
	 * Comparator<Order> { public int compare(Order p1, Order p2) { if
	 * (p1.GetOpenReferencePrice() < p2.GetOpenReferencePrice()) return -1; if
	 * (p1.GetOpenReferencePrice() > p2.GetOpenReferencePrice()) return 1; return 0;
	 * } }
	 */
	/*
	 * public static Comparator<Order> Price = new Comparator<Order>() {
	 * 
	 * double comparingDouble(Order s1, Order s2) {
	 * 
	 * double rollno1 = s1.GetOpenReferencePrice(); double rollno2 =
	 * s2.GetOpenReferencePrice();
	 * 
	 * 
	 * return rollno1-rollno2; }};
	 */
}

/*
 * # -*- coding: utf-8; py-indent-offset:4 -*-
 * #############################################################################
 * ## # # # Copyright (C) 2020 Achim Huebl # # #
 * #############################################################################
 * ##
 * 
 * # Order class that is suppose to inteface with the order classes of the
 * relevant brokers # the output of a trading algorithm shall just be a class
 * object that includes a # list of orders that are to be executed # A parser
 * then translates the order input to the respective API (backtrader, MT4 etc.)
 * # Various functionalities are already included - closure, close all, ids etc.
 * # A continuing process shall make these functions more efficient
 * 
 * from copy import copy import pytz from datetime import datetime as dt #import
 * math
 * 
 * class Order: def __init__(self, action:str, buySell:str, size:float,
 * underlying:str, priceDataFeed, limit:float = -1, takeProfit:float = -1,
 * stopLoss:float = -1, identifier1:int = 1, identifier2:int = 0, comment:str =
 * ''): this.action = action # OPEN or CLOSE this.type = buySell # BUY or SELL
 * this.status = 'PENDING' #PENING or FILLED this.volume = size this.underlying
 * = underlying # Just the ticker
 * 
 * this.limit = limit this.takeProfit = takeProfit this.stopLoss = stopLoss
 * 
 * this.openReferenceTime = priceDataFeed.livePrices[underlying]['time']
 * this.openReferencePrice = priceDataFeed.livePrices[underlying]['ask']
 * 
 * this.openFillTime = None this.openFillPrice = None
 * 
 * this.closeReferenceTime = None this.closeReferencePrice = None
 * 
 * this.closeFillTime = None this.closeFillPrice = None
 * 
 * this.comment = ''
 * 
 * this.identifier1 = identifier1 # first identifier for differenciating between
 * strategy parts this.identifier2 = identifier2 # second identifier for
 * differenciating between single orders
 * 
 * #@classmethod def PnL(self, priceDataFeed): if this.type == 'BUY': return
 * (priceDataFeed.livePrices[this.underlying]['bid'] - this.openFillPrice) *
 * this.volume elif this.type == 'SELL': return (this.openFillPrice-
 * priceDataFeed.livePrices[this.underlying]['ask']) * this.volume
 * 
 * #@classmethod def Close(self, priceDataFeed, limit:float = -1.0, comment:str
 * = ''): this.status = 'PENDING' this.comment = comment this.action = 'CLOSE'
 * this.closeReferenceTime = priceDataFeed.livePrices[this.underlying]['time']
 * if this.type == 'BUY': this.closeReferencePrice =
 * priceDataFeed.livePrices[this.underlying]['bid'] if this.type == 'SELL':
 * this.closeReferencePrice = priceDataFeed.livePrices[this.underlying]['ask']
 * return self
 * 
 * def GetPnLAtClose(self): if this.action == 'CLOSE': if this.type == 'BUY':
 * return (this.closeFillPrice - this.openFillPrice)*this.volume if this.type ==
 * 'SELL': return (this.openFillPrice - this.closeFillPrice)*this.volume else:
 * return 0.0
 * 
 * def PriceInfo(self): print(str(this.openFillTime) + "OPEN@"+
 * str(this.openFillPrice) + "\n" + str(this.closeFillTime) + "CLOSE@"+
 * str(this.closeFillPrice))
 * 
 * #@classmethod def GetIdentifiers(self): return (this.identifier1,
 * this.identifier2)
 * 
 * #@classmethod def ToZeroMQMT4Dictionary(self, slippage = 5): order_to_exec =
 * dict() order_to_exec['action'] = this.action order_to_exec['typeId'] = 0 if
 * this.type == 'BUY' else 1 order_to_exec['lots'] = this.volume
 * order_to_exec['symbol'] = this.underlying order_to_exec['SL'] = 0 if
 * this.stopLoss <= 0 else this.stopLoss order_to_exec['TP'] = 0 if
 * this.takeProfit <= 0 else this.stopLoss order_to_exec['comment'] =
 * this.comment order_to_exec['slippage'] = slippage return order_to_exec
 * 
 * def ToArray(self): return [this.action, this.type, this.identifier1,
 * this.identifier2, this.action, this.GetPnLAtClose()]
 * 
 * #fill orders based on given Price dictonary of the reference underlying def
 * Fill(self, priceDataFeed): if this.action == 'OPEN': this.openFillTime =
 * priceDataFeed.livePrices[this.underlying]['time'] if this.type == 'BUY':
 * this.status = 'FILLED' this.openFillPrice =
 * priceDataFeed.livePrices[this.underlying]['ask'] if this.type == 'SELL':
 * this.status = 'FILLED' this.openFillPrice =
 * priceDataFeed.livePrices[this.underlying]['bid'] if this.action == 'CLOSE':
 * print('close') this.closeFillTime =
 * priceDataFeed.livePrices[this.underlying]['time'] if this.type == 'BUY':
 * this.status = 'FILLED' this.closeFillPrice =
 * priceDataFeed.livePrices[this.underlying]['bid'] if this.type == 'SELL':
 * this.status = 'FILLED' this.closeFillPrice =
 * priceDataFeed.livePrices[this.underlying]['ask'] return self
 * 
 * def FillZeroMQ(self, response:dict): if this.action == 'OPEN':
 * this.openFillPrice = response['open_price'] this.openFillTime =
 * pytz.utc.localize(dt.strptime(response['open_time'], '%Y.%m.%d
 * %H:%M:%S')).astimezone(pytz.timezone('Europe/Berlin')) elif this.action ==
 * 'CLOSE': this.closeFillPrice = response['close_price'] this.closeFillTime =
 * pytz.utc.localize(dt.strptime(response['close_time'], '%Y.%m.%d
 * %H:%M:%S')).astimezone(pytz.timezone('Europe/Berlin')) this.status = 'FILLED'
 * #return self
 * 
 * 
 * #============================================================================
 * == # general methods
 * #============================================================================
 * == def CloseOrder(order:Order, priceDataFeed, limit:float = -1.0, comment:str
 * = ''): return copy(order).Close(priceDataFeed, limit, comment)
 * 
 * def FillOrder(order:Order, priceDataFeed): return
 * copy(order).Fill(priceDataFeed)
 * 
 * # remove the order where id1 and id 2 match def
 * RemoveOrderFromList(orderList:list, identifier1:int, identifier2:int): return
 * [x for x in orderList if ((x.identifier1 != identifier1) or (x.identifier2 !=
 * identifier2))]
 * 
 * # remove all orders where type and id1 match def
 * RemoveOrdersFromList(orderList:list, orderType:str, identifier1:int): return
 * [x for x in orderList if ((x.type != orderType) or (x.identifier1 !=
 * identifier1))]
 * 
 * # Returns the average prices of orders with matching identifier1 on the
 * buy-side def AveragePriceLong(orderList:list, identifier1:int): priceVol =
 * 0.0 vol = 0.0 ordersLong = [x for x in orderList if ((x.type == 'BUY' and
 * x.identifier1 == identifier1) and x.action == 'OPEN')] for order in
 * ordersLong: priceVol += order.openFillPrice * order.volume vol +=
 * order.volume if vol == 0.0: return 0.0 return priceVol / vol
 * 
 * def AveragePriceShort(orderList:list, identifier1:int): priceVol = 0.0 vol =
 * 0.0 ordersShort = [x for x in orderList if ((x.type == 'SELL' and
 * x.identifier1 == identifier1) and x.action == 'OPEN')] for order in
 * ordersShort: priceVol += order.openFillPrice * order.volume vol +=
 * order.volume if vol == 0.0: return 0.0 return priceVol / vol
 * 
 * # returns the order list where type (BUY or SELL) and identifier1 matches def
 * CloseAllOrders(orderType, price, orderList, identifier1, comment = ''):
 * toClose = [] for order in orderList: if order.identifier1 == identifier1 and
 * order.type == orderType: order = CloseOrder(order, price, comment)
 * toClose.append(order) return toClose
 * 
 * def CreatePriceDict(): return {'reference time': None, 'reference price':
 * None, 'fill time':None, 'fill price': None}
 */
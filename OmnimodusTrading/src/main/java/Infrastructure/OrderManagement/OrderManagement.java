//# -*- coding: utf-8; py-indent-offset:4 -*-
//###############################################################################
//#                                                                             #  
//#                          Copyright (C) 2020 Achim Huebl                     #
//#                                                                             #
//###############################################################################

package OrderManagement;

import Order.*;
import QuoteDataFeed.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;

public class OrderManagement {

	private Map<String, Map<String, ArrayList<Order>>> ordersPending;
	private Map<String, Map<String, ArrayList<Order>>> ordersActive;
	private Map<String, Map<String, ArrayList<Order>>> ordersClosed;
	private Map<String, Map<String, ArrayList<Order>>> ordersHist;
	private Map<String, Map<String, ArrayList<Order>>> temp;

	public Map<String, ArrayList<Order>> GetOrdersPending(String assetClass) {
		return this.ordersPending.get(assetClass);
	}

	public Map<String, ArrayList<Order>> GetOrdersActive(String assetClass) {
		return this.ordersActive.get(assetClass);
	}

	public Map<String, ArrayList<Order>> GetOrdersClosed(String assetClass) {
		return this.ordersClosed.get(assetClass);
	}

	public Map<String, ArrayList<Order>> GetOrdersHist(String assetClass) {
		return this.ordersHist.get(assetClass);
	}

	public OrderManagement(ArrayList<String> underlyings) {
		this.ordersActive = new HashMap<String, Map<String, ArrayList<Order>>>();
		this.ordersPending = new HashMap<String, Map<String, ArrayList<Order>>>();
		this.ordersClosed = new HashMap<String, Map<String, ArrayList<Order>>>();
		this.ordersHist = new HashMap<String, Map<String, ArrayList<Order>>>();
		this.temp = new HashMap<String, Map<String, ArrayList<Order>>>();

		this.ordersActive.put("FX", new HashMap<String, ArrayList<Order>>());
		this.ordersPending.put("FX", new HashMap<String, ArrayList<Order>>());
		this.ordersClosed.put("FX", new HashMap<String, ArrayList<Order>>());
		this.ordersHist.put("FX", new HashMap<String, ArrayList<Order>>());
		this.temp.put("FX", new HashMap<String, ArrayList<Order>>());

		if (underlyings.size() > 0)
			for (int i = 0; i < underlyings.size(); i++) {
				this.ordersActive.get("FX").put(underlyings.get(i), new ArrayList<Order>());
				this.ordersClosed.get("FX").put(underlyings.get(i), new ArrayList<Order>());
				this.temp.get("FX").put(underlyings.get(i), new ArrayList<Order>());
			}

	};

	// to make it easier, we use just this class to manage the orders
	// The function adds an order (OPEN or CLOSE) appropriately to the order lists
	public void AddOrder(Order order) {
		this.ordersHist.get(order.GetAssetClass()).get(order.GetUnderlying()).add(order); // order always gets added to
																							// order history
		if (order.GetAction() == "OPEN") // if the type is open, the order is active
		{
			if (order.GetStatus() == "FILLED") {
				this.ordersActive.get(order.GetAssetClass()).get(order.GetUnderlying()).add(order);

			} else if (order.GetStatus() == "PENDING") {

				this.ordersPending.get(order.GetAssetClass()).get(order.GetUnderlying()).add(order);
			}
		} else if (order.GetAction() == "CLOSE") // if the type is close, the order has to be removed from the active
													// list
			if (order.GetStatus() == "FILLED") {
				this.ordersActive.get(order.GetAssetClass()).get(order.GetUnderlying()).remove(
						Order.GetIndex(this.ordersActive.get(order.GetAssetClass()).get(order.GetUnderlying()), order));// =
																														// RemoveOrderFromList(this.ordersActive.get(order.GetAssetClass()).get(order.GetUnderlying()),
																														// order);
				this.ordersClosed.get(order.GetAssetClass()).get(order.GetUnderlying()).add(order);
			} else if (order.GetStatus() == "PENDING") {

				this.ordersPending.get(order.GetAssetClass()).get(order.GetUnderlying()).add(order);

			}
	}

	public void FillStatic() {
		for (String symbol : this.GetActiveSymbols()) {
			try {
				for (Order order : this.ordersActive.get("FX").get(symbol))

					order.FillStatic();
			} catch (Exception e) {
			}
		}
	}

	public ArrayList<String> GetActiveSymbols() {
		ArrayList<String> res = new ArrayList<String>();

		for (String symbol : this.ordersActive.keySet())
			res.add(symbol);
		return res;
	}

	public void FillAndFetch(String assetClass, Map<String, ArrayList<Order>> ordersToFill) {
		for (String underlying : ordersToFill.keySet()) {
			for (Order order : ordersToFill.get(underlying)) {
				order.FillStatic();
				if (order.GetAction() == "CLOSE") {
					Order.RemoveOrderFromList(this.ordersActive.get(assetClass).get(underlying), order);
					this.ordersClosed.get(assetClass).get(underlying).add(order);
				} else if (order.GetAction() == "OPEN") {
					this.ordersActive.get(assetClass).get(underlying).add(order);
				}
			}
		}
	}

	public double GetFloatingPnL(QuoteDataFeed quoteDataFeed) {
		double pnl = 0.0;

		for (String assetClass : this.ordersActive.keySet()) {
			for (String underlying : this.ordersActive.get(assetClass).keySet()) {
				for (Order order : this.ordersActive.get(assetClass).get(underlying)) {
					// if (order.GetFloatingPnL(quoteDataFeed) != 0)
					// System.out.print(
					// "yay, pnl: " + Double.toString(order.GetFloatingPnL(quoteDataFeed) * 100000)
					// + "\n");
					try {
						pnl += order.GetFloatingPnL(quoteDataFeed);
					} catch (Exception e) {
						System.out.print("Error calculating PnL!");
					}
				}
			}
		}

		return pnl * 100000;
	}

	public double GetClosedPnL() {

		double pnl = 0.0;
		for (String assetClass : this.ordersClosed.keySet()) {
			for (String underlying : this.ordersClosed.get(assetClass).keySet())
				for (Order order : this.ordersClosed.get(assetClass).get(underlying))
					pnl += order.GetClosePnLFX();
		}

		this.ordersClosed = new HashMap<String, Map<String, ArrayList<Order>>>(this.temp);
		return pnl;
	}

	public void Print() {
		for (String assetClass : this.ordersActive.keySet()) {
			for (String underlying : this.ordersActive.get(assetClass).keySet()) {
				for (Order order : this.ordersActive.get(assetClass).get(underlying)) {
					order.Print();
				}
			}
		}
	}

}
/*
 * # function to add new orders to the activeOrders and to the order history #
 * It removes closed orders from the active list and also def
 * IncludeNewOrders(self, orderDict: dict): for symbol in
 * list(orderDict.keys()): for order in orderDict[symbol]: this.AddOrder(order)
 * 
 * def FillAndInclude(self, orderDict: dict, priceDataFeed): for symbol in
 * list(orderDict.keys()): for order in orderDict[symbol]:
 * this.AddOrder(o.FillOrder(order, priceDataFeed))
 * 
 * # returns the floating pnl of the orders in teh ordersActive dictionary #
 * naturally, the input has to be a dictionary with symbols as key and # price
 * data (time, bid ask etc) dictionaries as values def GetFloatingPnL(self,
 * priceDataFeed): pnl = 0.0 for symbol in list(this.ordersActive.keys()): for
 * order in this.ordersActive[symbol]: pnl += order.PnL(priceDataFeed) return
 * pnl
 * 
 * # returns the closed pnl of the object # it also deletes the orders in the
 * orderHist in the process # that is to avoid having a too-large collection of
 * order objects def RetrieveClosedPnL(self): pnl = 0.0 for symbol in
 * list(this.ordersClosed.keys()): for order in this.ordersClosed[symbol]: pnl
 * += order.GetPnLAtClose() this.ordersClosed[symbol] = [] return pnl
 * 
 * #============================================================================
 * = #================ closure structure of orders ===================
 * #============================================================================
 * = # Close all orders where identifier matches with the input def
 * CloseAllOrders(self, underlying:str, orderType:str, identifier1:int,
 * priceDataFeed, comment:str = ''): return {underlying:
 * o.CloseAllOrders(orderType, priceDataFeed, this.ordersActive[underlying],
 * identifier1, comment)}
 * 
 * # returns the close command for the given number of best and worst orders
 * based on the input # with the identifier1 matching def CloseBestNWorst(self,
 * underlying:str, orderType, identifier1, noBest, noWorst, priceDataFeed,
 * comment = ''): orderList = [x for x in this.ordersActive[underlying] if
 * (x.type == orderType and x.identifier1 == identifier1)] if len(orderList) <=
 * min(noBest, noWorst): this.CloseAllOrders(underlying, orderType, identifier1,
 * priceDataFeed) else: # otherwise, we have eventually trim the numbers and the
 * combine the other functions temp = TrimBestWorst(noBest, noWorst,
 * len(orderList)) tempBest = temp[0] tempWorst = temp[1] return {underlying :
 * (this.CloseBestOrders(underlying, orderType, identifier1, tempBest,
 * priceDataFeed, comment)[underlying] + this.CloseWorstOrders(underlying,
 * orderType, identifier1, tempWorst, priceDataFeed, comment)[underlying])}
 * 
 * return {underlying: []}
 * 
 * # returns the list of closure commands of the best active orders under the
 * ones where identifier1 matches def CloseBestOrders(self, underlying,
 * orderType, identifier1, noBest, priceDataFeed, comment = ''): returnList = []
 * #filter the list orderList = [x for x in this.ordersActive[underlying] if
 * (x.type == orderType and x.identifier1 == identifier1)] if len(orderList) <=
 * noBest: # no orders open, return empty list return
 * this.CloseAllOrders(underlying, orderType, identifier1, priceDataFeed,
 * comment) # sort the list and return the value
 * 
 * # Case BUY if orderType == 'BUY': # reverse = True means descending,
 * therefore we pick the first elements orderList.sort(key = lambda x:
 * x.openFillPrice, reverse = True) ordersSorted = orderList for orderIndex in
 * range(0, noBest): orderToClose = o.CloseOrder(order =
 * ordersSorted[orderIndex], priceDataFeed = priceDataFeed, comment = comment)
 * returnList.append(orderToClose) return {underlying : returnList} #case SELL
 * if orderType == 'SELL': # ascending, the best oneshave high prices
 * orderList.sort(key = lambda x: x.openFillPrice, reverse = False) ordersSorted
 * = orderList for orderIndex in range(0, noBest): orderToClose =
 * o.CloseOrder(order = ordersSorted[orderIndex], priceDataFeed = priceDataFeed,
 * comment = comment) returnList.append(orderToClose) return {underlying :
 * returnList}
 * 
 * return {underlying: []}
 * 
 * # same function as above, just the reverse parameter in sort is inverted def
 * CloseWorstOrders(self, underlying, orderType, identifier1, noWorst,
 * priceDataFeed, comment = ''): returnList = [] #filter the list orderList = [x
 * for x in this.ordersActive[underlying] if (x.type == orderType and
 * x.identifier1 == identifier1)] if len(orderList) == 0 or
 * len(this.ordersActive) == 0: return [] if len(orderList) <= noWorst: # no
 * orders open, return empty list return this.CloseAllOrders(underlying,
 * orderType, identifier1, priceDataFeed, comment) # sort the list and return
 * the value
 * 
 * # Case BUY if orderType == 'BUY': # reverse = True means descending,
 * therefore we pick the first element orderList.sort(key = lambda x:
 * x.openFillPrice, reverse = False) ordersSorted = orderList for orderIndex in
 * range(0, noWorst): orderToClose = o.CloseOrder(order =
 * ordersSorted[orderIndex], priceDataFeed = priceDataFeed, comment = comment)
 * returnList.append(orderToClose) return {underlying : returnList} #case SELL
 * elif orderType == 'SELL': # ascending, the best oneshave high prices
 * orderList.sort(key = lambda x: x.openFillPrice, reverse = True) ordersSorted
 * = orderList for orderIndex in range(0, noWorst): orderToClose =
 * o.CloseOrder(order = ordersSorted[orderIndex], priceDataFeed = priceDataFeed,
 * comment = comment) returnList.append(orderToClose) return {underlying :
 * returnList}
 * 
 * return {}
 * 
 * #============================================================================
 * = #================ calculation of psoition sizes and prices
 * ===================
 * #============================================================================
 * =
 * 
 * # calculate average price calculation absed on id1 orders def
 * AveragePriceLong(self, underlying, identifier1): return
 * o.AveragePriceLong(this.ordersActive[underlying], identifier1)
 * 
 * def AveragePriceShort(self, underlying, identifier1): return
 * o.AveragePriceShort(this.ordersActive[underlying], identifier1)
 * 
 * # get the total net exposure of the active order list def
 * GetNetExposure(self, underlying): netExposure = 0.0 for order in
 * this.ordersActive[underlying]: if order.type == 'BUY': netExposure +=
 * order.volume elif order.type =='SELL': netExposure -= order.volume return
 * netExposure
 * 
 * #calculate the total exposure of the active order list EXCLUDING the ones
 * with # identifier1 mathcing def GetCleanExposure(self, underlying,
 * identifier1): localExposure = 0.0 for order in this.ordersActive[underlying]:
 * if order.type == 'BUY' and order.identifier1 == identifier1: localExposure +=
 * order.volume elif order.type =='SELL' and order.identifier1 == identifier1:
 * localExposure -= order.volume return this.GetNetExposure(underlying) -
 * localExposure
 * 
 * def GetPositionLong(self, underlying, identifier1): vol = 0 for order in
 * this.ordersActive[underlying]: if (order.type == 'BUY') and
 * (order.identifier1 == identifier1): vol += order.volume return vol
 * 
 * def GetPositionShort(self, underlying, identifier1): vol = 0 for order in
 * this.ordersActive[underlying]: if (order.type == 'SELL') and
 * (order.identifier1 == identifier1): vol += order.volume return vol
 * 
 * 
 * def TrimBestWorst(noBest, noWorst, total): tempBest = noBest tempWorst =
 * noWorst if noBest + noWorst <= total: return [noBest, noWorst] else: for i in
 * range(0,total): tempWorst -= 1 tempBest -=1 if tempBest + tempWorst <= total:
 * return [tempBest, tempWorst]
 */
package AccountManagement;

import OrderManagement.*;
import QuoteDataFeed.*;

public class AccountManagement {

	private double balance;
	private double equity;
	private double floatingPnL;
	private double margin;

	public AccountManagement(double balance) {
		this.balance = balance;
		this.equity = 0.0;
		this.margin = 0.0;
		this.floatingPnL = 0.0;
	}

	public void FetchData(OrderManagement orderManagement, QuoteDataFeed priceDataFeed) {

		double newFloatPnL = orderManagement.GetFloatingPnL(priceDataFeed);
		double changePnL = newFloatPnL - this.floatingPnL;
		this.floatingPnL = newFloatPnL;
		double closedPnL = orderManagement.GetClosedPnL();
		this.balance += closedPnL;
		this.equity = this.balance + this.floatingPnL;
		this.margin = 0.0; // tbu
	}
	
	public void Print()
	{
		System.out.print("{equity: " + Double.toString(this.balance) + ", balance: " + Double.toString(this.balance) + "}\n"); 
	}

	/*
	 * # TODO: Function that does the order filling and pnl calculation at once # to
	 * be a little more efficient def FetchAndFill(self,
	 * orderManagement:om.OrderManagement, priceDataFeed:PriceDataFeed): return 0
	 */
}

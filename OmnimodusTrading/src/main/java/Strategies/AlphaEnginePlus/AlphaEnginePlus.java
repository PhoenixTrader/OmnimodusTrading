package Strategies.AlphaEnginePlus;

import DataFeed.QuoteDataFeed.*;
import Infrastructure.OrderManagement.*;

import Strategies.AlphaEnginePlus.*;

import Strategies.Measures.*;
import Infrastructure.Order.Order;

import java.util.Arrays;
import java.util.Map;
import java.util.ArrayList;
import java.util.*;


//+------------------------------------------------------------------+
//|   The class that combines the coast trading strategies.          |
//+------------------------------------------------------------------+
public class AlphaEnginePlus 
	  {
	
	   //--- print management
	private String            file;
	private int               file_handle;
	   //--- measure definer
	private String            measure;
	   //-- the symbol to be traded
	private String            underlying;
	   //--- slippage allowance
	   //--- thresholds and position sizes
	private double            deltas[]; //--- the deltas barriers for the coast trading strats
	private double            positionSizes[]; //--- the position sizes for each strat
	   //--- global liquidity
	//private  GlobalLiquidity   globalLiquidity; //--- global liquidity class object - we calculate the values for the whole
	   //--- coast trading class array for the strategy execution
	private  ArrayList<CoastTrading>      coastTradingPairs;   //--- long trading strats
	   //--- exposure management field

	private ExposureManagement exposureManagement;
	   //--- default constructor
	public AlphaEnginePlus() {};



	//+------------------------------------------------------------------+
	//|   Class constructor, initializes all trading variables.          |
	//+------------------------------------------------------------------+
	  public AlphaEnginePlus(String underlying, double deltaArray[], double deltaOvershootScale[],
              double positionSizeArray[], double exposureBarrierLevels[],
              String measureThresholds, String fileID, QuoteDataFeed quoteDataFeed)
	  {
	  //--- define the filename for te .csv; that is very important as these files should be read to python
	   this.file ="AlphaEngineFlex" + "//" +underlying + "_"+ fileID + ".csv";

	   this.measure = measureThresholds;
	   this.underlying = underlying;
	   int nbOfCoastTraders = deltaArray.length;
	   this.deltas = deltaArray;
	   this.positionSizes =  positionSizeArray;

	   double marginBarriers[] = new double[3];

	   marginBarriers[0] = 5.0;
	   marginBarriers[1] = 10.0;
	   marginBarriers[2] = 15.0;
	   ArrayList<Double> eB = new ArrayList<Double>();
	   ArrayList<Double> mB = new ArrayList<Double>();
	   
	   for(int j = 0; j < exposureBarrierLevels.length; j++)
		   eB.add(exposureBarrierLevels[j]);
	   
	   this.exposureManagement = new ExposureManagement(nbOfCoastTraders, eB, mB);

	   this.coastTradingPairs = new ArrayList<CoastTrading>();


	   for(int i = 0; i < nbOfCoastTraders; ++i)
	     {
	      //--- define trading strategies
	      coastTradingPairs.add(
	    		  new CoastTrading(underlying, 
	    		  deltas[i], deltas[i], deltas[i],  deltaOvershootScale[i] * deltas[i], deltaOvershootScale[i] * deltas[i],
	    		  i + 1, positionSizes[i], measureThresholds,
	    		  exposureManagement, quoteDataFeed)
	    		  );
	      System.out.print("Initialized pair " + Integer.toString(i) + " with delta: " + Double.toString(deltas[i])+"\n");
	     }
	   
	   //this.exposureManagement.CalculateNetExposure();

	// calculate global liquidity for the first value, deltas should be equidistant to represent
	// unlikeliness not based on individual selection of deltas
	   int gridSize = 200; //--- the value that shall determine the amount of 5bps steps for the grid to be analyzed on each step
	   int rollingEvents = 100;
	   int liquidityGridSize = 12;//--- gridsize for the approximation of the global liquidity
	   double deltaGrid[] = new double[gridSize];
	   for(int  i = 0; i < gridSize; i++)//--- we use here a specific grid for the global liquidity
	      deltaGrid[i] = 0.025 / 100.0 + 0.05 / 100.0 * (double)i;

	   //globalLiquidity = GlobalLiquidity(deltaGrid, liquidityGridSize, rollingEvents, measureThresholds);

	//globalLiquidity = GlobalLiquidity(deltas, ArraySize(deltas), rollingEvents, measureThresholds);
	//--- we initialize the liquidity value with historic data from iClose
	   int lookbackFrame = 8640; //--- 1 month lookback if 5 min selected
	   int timeframe = 5;
	   double spread = 0.00015;

	   //if(globalLiquidity.InitializeLiquidity(spread, timeframe, lookbackFrame))
	   //   Print("Global Liquidity Initialized with value " + DoubleToStr(globalLiquidity.liquidity));
	  }

	  
	  
	//+------------------------------------------------------------------+
	//|   Core trading function, loops through the trading pairs and     |
	//|   executes the asymmetric trade strategy.                        |
	//+------------------------------------------------------------------+
	  public Map<String, ArrayList<Order>> TradeAsymmetric(OrderManagement orderManagement, QuoteDataFeed quoteDataFeed)
	  {
	//--- update global liquidity
	   //UpdateLiquidity();
	//--- trading execution
		Map<String, ArrayList<Order>> orders = new HashMap<String, ArrayList<Order>>();
		  
		orders.put(this.underlying, new ArrayList<Order>());
	   for(int i = 0; i < coastTradingPairs.size(); ++i)
	     {
	      //--- trade execution
	      orders.get(this.underlying).addAll(coastTradingPairs.get(i).TradeAsymmetric(quoteDataFeed, this.exposureManagement, orderManagement).get(this.underlying)); //---

	      //--- The following function is for debugging purposes: It checks if the average prices are calculated correctly
	      //CheckAverages(i)
			if(!orders.get(this.underlying).isEmpty())
				System.out.print("stop");
	      //--- closure of positions (except for decascading)
	      orders.get(this.underlying).addAll(coastTradingPairs.get(i).TakeProfit(quoteDataFeed, this.exposureManagement, orderManagement).get(this.underlying));

	     }//--- end for loop through coast trading pairs

	   //datetime time = TimeCurrent();
	   //if((TimeHour(time) == 12 || TimeHour(time) == 0) && TimeMinute(time)== 0 && TimeSeconds(time) == 0)
	    //  Print("Net exposure is " + DoubleToStr(exposureManagement.GetNetExposure()));

	   return orders;
	  }





	  public void FetchQuoteData(QuoteDataFeed quoteDataFeed)
	  {
		for(CoastTrading coastTradingPair: this.coastTradingPairs)
			coastTradingPair.FetchQuoteData(quoteDataFeed);
	  }
	//+------------------------------------------------------------------+
	//|   The function that updates the liquidity. Commented sections    |
	//|   were just for printing information or the use of different     |
	//|   data fields.                                                   |
	//+------------------------------------------------------------------+
	/*void UpdateLiquidity()
	  {
	//double liqEMAOld = globalLiquidity.liqEMA;
	//double liqOld = globalLiquidity.liquidity;
	   if(globalLiquidity.Trigger()) //--- note that this updates the whole array of current events
	      if(globalLiquidity.ComputeLiquidity())
	        {
	         //--- It's a lil annoying to have the thing printed out all the time
	         //Print("Gobal liquidity updated: " +DoubleToStr(globalLiquidity.liquidity));
	        }

	   
	   double upBarrier = 0.75;
	   double downBarrier = 1.0 - upBarrier;
	   if(liqOld > downBarrier && globalLiquidity.liquidity < downBarrier)
	     Print("Global liquidity value below " + DoubleToStr(NormalizeDouble(downBarrier, 2)) + " now: " + DoubleToStr(globalLiquidity.liquidity));
	   if(liqOld < downBarrier && globalLiquidity.liquidity > downBarrier)
	     Print("Global liquidity value above " + DoubleToStr(NormalizeDouble(downBarrier, 2)) + " now: " + DoubleToStr(globalLiquidity.liquidity));
	   if(liqOld > upBarrier && globalLiquidity.liquidity < upBarrier)
	     Print("Global liquidity value below " + DoubleToStr(NormalizeDouble(upBarrier, 2)) + " now: " + DoubleToStr(globalLiquidity.liquidity));
	   if(liqOld < upBarrier && globalLiquidity.liquidity > upBarrier)
	     Print("Global liquidity value above " + DoubleToStr(NormalizeDouble(upBarrier, 2)) + " now: " + DoubleToStr(globalLiquidity.liquidity));
	   
	  }*/
	//+------------------------------------------------------------------+


	//+------------------------------------------------------------------+
	//|   Close the worst out of all trades                              |
	//+------------------------------------------------------------------+
	/*void CloseWorst()
	  {

	//double performance = 0.0;
	   int orderTicket = -1;
	   double worstPnL =0.0;
	   int totalNumberOfOrders = OrdersTotal();

	   for(int positionIndex = totalNumberOfOrders - 1; positionIndex >= 0 ; positionIndex --)  //  <-- for loop to loop through all Orders . .   COUNT DOWN TO ZERO !
	     {
	      if(!OrderSelect(positionIndex, SELECT_BY_POS, MODE_TRADES))
	         continue;   // <-- if the OrderSelect fails advance the loop to the next PositionIndex

	      if(OrderSymbol() == fxRate)             //--- match sell orders with the short strategy
	        {
	         if(OrderProfit() < worstPnL)
	           {
	            worstPnL = OrderProfit();
	            orderTicket = OrderTicket();
	           }

	        }
	     }//--- end for loop

	   if(orderTicket >=0)
	      if(OrderSelect(orderTicket,SELECT_BY_TICKET))
	        {
	         if(OrderProfit() < 0.0)
	           {
	            double lots = OrderLots();
	            int index = (int)MathAbs(OrderMagicNumber())  - 1;
	            boolean longPosition = OrderMagicNumber() > 0;
	            double price = OrderOpenPrice();
	            if(OrderClose(OrderTicket(), OrderLots(), Bid, slippage))
	              {
	               Print("Closed worst long position " + IntegerToString(index + 1));
	               coastTradingPairs[index].UpdateAverageClose(lots, price, longPosition);
	              }
	           }
	        }

	  }*/
	//+------------------------------------------------------------------+


	//+------------------------------------------------------------------+
	//|   Data print funtion. NOTE: has to be changed in accordance to   |
	//|   the data header printer.                                       |
	//+------------------------------------------------------------------+
	/*void PrintData()
	  {
	   if(file_handle!=INVALID_HANDLE)
	     {
	      FileWrite(file_handle,TimeCurrent(), Bid, Ask, globalLiquidity.liquidity, AccountBalance(), AccountEquity(), exposureManagement.GetNetExposure(), OrdersTotal());
	      //FileWrite(file_handle,TimeCurrent(), Ask, globalLiquidity.liquidity, AccountBalance(), AccountEquity(), exposureManagement.GetNetExposure(),
	      //globalLiquidity.GetDCPercentage(deltas[0]),globalLiquidity.GetDCPercentage(deltas[1]),
	      //globalLiquidity.GetDCPercentage(deltas[2]),globalLiquidity.GetDCPercentage(deltas[3]));
	     }
	   else
	      PrintFormat("Failed to open %s file, Error code = %d",file,GetLastError());

	  }*/
	//+------------------------------------------------------------------+


	//+------------------------------------------------------------------+
	//|   Function that prints the .csv header. Has to be selected       |
	//|   such that the data and headers match.                          |
	//+------------------------------------------------------------------+
	/*void PrintHeader()
	  {
	   file_handle=FileOpen(file,FILE_READ|FILE_WRITE|FILE_CSV);
	   if(file_handle!=INVALID_HANDLE)
	     {
	      FileWrite(file_handle,"Time", "Bid", "Ask", "Global Liquidity", "Balance", "Equity", "Exposure", "Order count");
	      //FileWrite(file_handle,"Time", "Ask", "Global Liquidity",  "Balance", "Equity", "Exposure", "DC percentage 1", "DC percentage 2", "DC percentage 3", "DC percentage 4");
	     }
	   else
	      PrintFormat("Failed to open %s file, Error code = %d",file,GetLastError());
	  }*/
	//+------------------------------------------------------------------+


	//+------------------------------------------------------------------+
	//|   File close function, only for deinit function.                 |
	//+------------------------------------------------------------------+
	/*void CloseFile()
	  {

	   if(file_handle!=INVALID_HANDLE)
	      FileClose(file_handle);
	   else
	      PrintFormat("Failed to open %s file, Error code = %d",file,GetLastError());

	  }*/
	//+------------------------------------------------------------------+

	//+------------------------------------------------------------------+
	//|   Function that writes error message if average price is wrong.  |
	//+------------------------------------------------------------------+
	/*void CheckAverages(int i)
	  {
	   double avgLong = 0, avgShort = 0;
	   AveragePrice(avgLong, avgShort, i + 1);

	//--- check if the price averages are calculated correctly
	   if(MathAbs(avgLong - coastTradingPairs[i].GetAveragePrice(true)) > 1e-4)
	      Print("avg Long price is off");

	   if(MathAbs(avgShort - coastTradingPairs[i].GetAveragePrice(false)) > 1e-4)
	      Print("avg Short price is off");
	  }*/
	//+------------------------------------------------------------------+


	//====================================================================
	//=== not in use, but eventually useful later on =====================
	//====================================================================

	//+------------------------------------------------------------------+
	//|                                                                  |
	//+------------------------------------------------------------------+
	/*boolean CrossClose(int index)
	  {

	   if(coastTradingPairs[index].ProfitCondition(true))
	     {
	      coastTradingPairs[index].CloseAllOrders(true);
	      CloseWorst();

	      return true;
	     }

	   if(coastTradingPairs[index].ProfitCondition(false))
	     {
	      coastTradingPairs[index].CloseAllOrders(false);
	      CloseWorst();

	      return true;
	     }

	   return false;
	  }

*/
	//+------------------------------------------------------------------+
	//|                                                                  |
	//+------------------------------------------------------------------+
	/*void ResetStrategy(void)
	  {

	   for(int i = 0; i < ArraySize(coastTradingPairs); i++)
	      coastTradingPairs[i].CloseStrategy();

	   for(int i = 0; i < ArraySize(coastTradingPairs); ++i)
	     {
	      //--- define trading strategies
	      double deltaLong = deltas[i];
	      double deltaShort = deltas[i]; //i == nbOfCoastTraders - 1 ? deltas[i] : (deltas[i] + deltas[i + 1]) / 2.0;
	      //coastTradingPairs[i] = CoastTrading(fxRate, deltaLong, deltaLong, deltaLong, i + 1, slippage, positionSizes[i], measure);
	      Print("Initialized pair " + IntegerToString(i) + " with delta: " + DoubleToStr(deltas[i]));
	     }

	  }*/
	//+------------------------------------------------------------------+

}

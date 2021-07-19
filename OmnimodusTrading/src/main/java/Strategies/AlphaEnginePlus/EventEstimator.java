package AlphaEnginePlus;

import java.io.*;
import java.time.ZonedDateTime;

import Measures.iMeasure;
import QuoteDataFeed.*;
import Quote.*;

public class EventEstimator {
	private String symbol;
	private double prevExtreme;
	private ZonedDateTime prevExtremeTime;

	private double prevDC;
	private ZonedDateTime prevDCTime;

	private double extreme;
	private ZonedDateTime extremeTime;

	private double deltaUp;
	private double deltaDown;
	private double deltaStarUp, deltaStarDown;
	private double osL;
	private int marketMode;
	private boolean initalized;
	private double reference;

	public EventEstimator() {
		initalized = false;
	};

	public int GetMarketMode() {
		return this.marketMode;
	}

	// ==========================================================================================
	// Functions to estimate the event based on the paper. These are coded as
	// follows:
	// 0 : no event
	// 1 : directional change to up
	// 2 : overshoot upwards
	// -1 : directional change to down
	// -2 : overshoot downwards
	// ============================================================================================
	// int EstimateEvent(iMeasure &measureFunc);
	// int EstimateEventInit(double bid, double ask, iMeasure &measureFunc);

	// --- Get functions which return the respective values
	public double GetDeltaUp() {
		return this.deltaUp;
	}

	public double GetDeltaDown() {
		return this.deltaDown;
	}

	public void Print()
	{
		System.out.print("MarketMode: " + Integer.toString(this.marketMode) +", Reference:" + Double.toString(this.reference) + ",\nExtreme:" + Double.toString(this.extreme) + ", prevExtreme: " + Double.toString(this.prevExtreme)+"\n");
	}
	// --- Set functions
	// double SetReferencePrice(double refPrice){reference = refPrice;}

	// --- Initialization functions to initialize the values based on histporical
	// data
	// boolean InitializeTracker(double spread, int timeframe, int lookbackFrame,
	// iMeasure &measureFunc, LocalLiquidity &localLiquidity);
	// boolean InitializeTrackerRaw(double spread, int timeframe, int lookbackFrame,
	// iMeasure &measureFunc);
	// };

	// +------------------------------------------------------------------+
	// | Constructor. |
	// +------------------------------------------------------------------+
	public EventEstimator(String symbol, double threshUp, double threshDown, double dStarUp, double dStarDown,
			QuoteDataFeed quoteDataFeed) {
		this.symbol = symbol;
		double priceMid = 1.0;
		try {
		 priceMid = quoteDataFeed.GetQuote("FX", symbol).GetMid();
		} catch(Exception e){
		}
		this.prevExtreme = priceMid;
		this.prevExtremeTime = ZonedDateTime.now();
		this.prevDC = priceMid;
		this.prevDCTime = ZonedDateTime.now();
		this.extreme = priceMid;
		this.extremeTime = ZonedDateTime.now();
		this.reference = priceMid;
		this.deltaStarUp = dStarUp;
		this.deltaStarDown = dStarDown;

		this.marketMode = -1;
		this.deltaUp = threshUp;
		this.deltaDown = threshDown;
		this.osL = 0.0;
		this.initalized = true;
	}

	// +------------------------------------------------------------------+
	// | Estimates event based on measure provided according to the |
	// | notes. |
	// +------------------------------------------------------------------+
	public int EstimateEvent(QuoteDataFeed quoteDataFeed, iMeasure measureFunc) {
		if (this.marketMode == -1) {
			if (measureFunc.Change(quoteDataFeed.GetQuote("FX", this.symbol).GetBid(), this.extreme) >= this.deltaUp) {
				this.prevExtreme = extreme;
				this.prevExtremeTime = extremeTime;
				this.marketMode = 1;
				this.extreme = quoteDataFeed.GetQuote("FX", this.symbol).GetAsk();
				this.extremeTime = quoteDataFeed.GetQuote("FX", this.symbol).GetTime();
				this.prevDC = quoteDataFeed.GetQuote("FX", this.symbol).GetAsk();

				this.prevDCTime = quoteDataFeed.GetQuote("FX", this.symbol).GetTime();
				this.reference = quoteDataFeed.GetQuote("FX", this.symbol).GetAsk();
				return 1;
			}
			if (quoteDataFeed.GetQuote("FX", this.symbol).GetAsk() < this.extreme) {
				this.extreme = quoteDataFeed.GetQuote("FX", this.symbol).GetAsk();
				this.extremeTime = quoteDataFeed.GetQuote("FX", this.symbol).GetTime();

				if (measureFunc.Change(this.extreme, this.reference) <= -this.deltaStarUp) {
					this.reference = this.extreme;
					return -2;
				}
				return 0;
			}
		} else {
			if (this.marketMode == 1) {
				if (measureFunc.Change(quoteDataFeed.GetQuote("FX", this.symbol).GetAsk(),
						this.extreme) <= -this.deltaDown) {
					this.prevExtreme = this.extreme;
					this.prevExtremeTime = this.extremeTime;
					this.marketMode = -1;
					this.extreme = quoteDataFeed.GetQuote("FX", this.symbol).GetBid();
					this.extremeTime = quoteDataFeed.GetQuote("FX", this.symbol).GetTime();
					this.prevDC = quoteDataFeed.GetQuote("FX", this.symbol).GetBid();
					this.prevDCTime = quoteDataFeed.GetQuote("FX", this.symbol).GetTime();
					this.reference = quoteDataFeed.GetQuote("FX", this.symbol).GetBid();
					return -1;
				}
				if (quoteDataFeed.GetQuote("FX", this.symbol).GetBid() > this.extreme) {
					this.extreme = quoteDataFeed.GetQuote("FX", this.symbol).GetBid();
					this.extremeTime = quoteDataFeed.GetQuote("FX", this.symbol).GetTime();

					if (measureFunc.Change(extreme, reference) >= this.deltaStarDown) {
						this.reference = this.extreme;
						return 2;
					}
					return 0;
				}
			}
		}
		return 0;
	}
	// +------------------------------------------------------------------+

	// +------------------------------------------------------------------+
	// | Adjusted variant of the estimator for the use of historical |
	// | data for the initialization. |
	// +------------------------------------------------------------------+
	public int EstimateEventInit(double bid, double ask, iMeasure measureFunc) {
		if (this.marketMode == -1) {
			if (measureFunc.Change(bid, this.extreme) >= this.deltaUp) {
				this.prevExtreme = this.extreme;
				this.prevExtremeTime = this.extremeTime;
				this.marketMode = 1;
				this.extreme = ask;
				this.prevDC = ask;
				this.reference = ask;
				return 1;
			}
			if (ask < this.extreme) {
				this.extreme = ask;
				if (measureFunc.Change(this.extreme, this.reference) <= -this.deltaStarUp) {
					this.reference = this.extreme;
					return -2;
				}
				return 0;
			}
		} else if (this.marketMode == 1) {
			if (measureFunc.Change(ask, this.extreme) <= -this.deltaDown) {
				this.prevExtreme = this.extreme;
				this.prevExtremeTime = this.extremeTime;
				this.marketMode = -1;
				this.extreme = bid;
				this.prevDC = bid;
				this.reference = bid;
				return -1;
			}
			if (bid > this.extreme) {
				this.extreme = bid;
				if (measureFunc.Change(this.extreme, this.reference) >= this.deltaStarDown) {
					this.reference = this.extreme;
					return 2;
				}
				return 0;
			}
		}
		return 0;
	}


	public void FetchQuoteData(QuoteDataFeed quoteDataFeed)
	{
		this.reference =this.extreme =this.prevDC =this.prevExtreme = quoteDataFeed.GetQuote("FX", symbol).GetMid();
		this.extremeTime = this.prevDCTime = this.prevExtremeTime =quoteDataFeed.GetQuote("FX", symbol).GetTime();
	}

}
/*
 * //+------------------------------------------------------------------+ //|
 * Function to initialize the fields (extreme, marketMode etc) | //| based on
 * historical data. |
 * //+------------------------------------------------------------------+ public
 * boolean InitializeTracker(double spread, int timeframe, int lookbackFrame,
 * iMeasure &measureFunc, LocalLiquidity &localLiquidity) { for(int i =
 * lookbackFrame; i >=0; i--) { //--- calculate event int event =
 * EstimateEventInit(iClose(Symbol(), timeframe, i) - spread / 2.0,
 * iClose(Symbol(), timeframe, i) + spread / 2.0, measureFunc); //--- provide
 * event to liquidity localLiquidity.ComputeLiquidityExternal(event); }
 * 
 * //reference = GetReferencePrice(timeframe,lookbackFrame);// return true; }
 * 
 * //+------------------------------------------------------------------+
 * 
 * //+------------------------------------------------------------------+ //|
 * Initialize event tracker without a corresponding liquidity. |
 * //+------------------------------------------------------------------+ public
 * boolean InitializeTrackerRaw(double spread, int timeframe, int lookbackFrame,
 * iMeasure &measureFunc) { for(int i = lookbackFrame; i >=0; i--)
 * EstimateEventInit(iClose(Symbol(), timeframe, i) - spread / 2.0,
 * iClose(Symbol(), timeframe, i) + spread / 2.0, measureFunc); return true; }
 * //+------------------------------------------------------------------+
 * 
 * 
 * public double GetReferencePrice(int timeframe, int lookbackFrame) { double
 * price_max = Ask; double price_min = Bid;
 * 
 * double differences_up_to_down[]; double differences_down_to_up[]; double
 * closePrices[]; for(int i = lookbackFrame; i >=0; i--) { price_max =
 * MathMax(price_max, iClose(Symbol(), timeframe, i)); price_min =
 * MathMin(price_min, iClose(Symbol(), timeframe, i)); }
 * //Functions::PushBackInt(differences_up_to_down, draw_down_high -
 * iClose(Symbol(), timeframe, i));
 * //Functions::PushBackInt(differences_down_to_up, draw_high:llow -
 * iClose(Symbol(), timeframe, i)); //Functions::PushBackDouble(closePrices,
 * iClose(Symbol(), timeframe, i));
 * 
 * if(price_max - Bid > Ask - price_min) return price_max; else return
 * price_min; };
 * 
 * }
 */

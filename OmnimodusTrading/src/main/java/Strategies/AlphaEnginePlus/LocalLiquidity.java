package Strategies.AlphaEnginePlus;

import DataFeed.QuoteDataFeed.*;
import Strategies.Measures.*;

//+------------------------------------------------------------------+
//|   The local liquidity class, the liquidity for a single          |
//|   threshold, just according to the notes.                        |
//+------------------------------------------------------------------+
public class LocalLiquidity {

	private String underlying;
	private double surpDC, surpOS; // --- surprise single value for overshoot and directional change
	private double surp, upSurp, downSurp;
	private double liq, upLiq, downLiq;
	private double H1, H2;
	private double deltaUp, deltaDown;
	private double delta;
	private double extreme, dStar, reference;
	private int marketMode;
	private double alpha, alphaWeight;
	private boolean initalized;
	private double liquidityUp;
	private double liquidity;
	private double liquidityDown;

	public LocalLiquidity() {
		initalized = false;
	};

	// --- just returns the liquidity as we prefer to keep fields private
	public double GetLiquidity() {
		return liquidity;
	}

//+------------------------------------------------------------------+
//|   Simple constructor, calculates H1 and H2 and copy-pastes       |
//|   input to fields.                                               |
//+------------------------------------------------------------------+
	public LocalLiquidity(String underlying, double d, double dUp, double dDown, double dS, double a,
			QuoteDataFeed quoteDataFeed) {
		this.underlying = underlying;
		this.deltaUp = dUp;
		this.deltaDown = dDown;
		this.delta = d;
		this.marketMode = -1;
		try {
			this.extreme = quoteDataFeed.GetQuote("FX", underlying).GetMid();
			this.reference = quoteDataFeed.GetQuote("FX", underlying).GetMid();
		} catch (Exception e) {
			this.extreme = 1.0;
			this.reference = 1.0;

		}
		this.dStar = dS;
		this.initalized = true;
		this.alpha = a;
		this.alphaWeight = Math.exp(-2.0 / (a + 1.0));
		this.computeH1H2exp();
//--- calculation of expectation of -log( conditinal probability of transition)
		this.surpOS = 2.0 * this.dStar / (this.deltaDown + this.deltaUp); // --- = 2.525729 in the optimized case
		this.surpDC = -Math.log(1 - Math.exp(-2.0 * this.dStar / (this.deltaDown + this.deltaUp))); // --- = 0.08338161
																									// in the optimized
																									// case
	}
//+------------------------------------------------------------------+

//+------------------------------------------------------------------+
//|   Calculates H1 and H2 according to the formula in the notes.    |
//+------------------------------------------------------------------+
	private boolean computeH1H2exp() {
		this.H1 = -Math.exp(-this.dStar / this.delta) * Math.log(Math.exp(-this.dStar / this.delta))
				- (1.0 - Math.exp(-this.dStar / this.delta)) * Math.log(1.0 - Math.exp(-this.dStar / this.delta));
		this.H2 = Math.exp(-this.dStar / this.delta) * Math.pow(Math.exp(Math.exp(-this.dStar / this.delta)), 2.0)
				- (1.0 - Math.exp(-this.dStar / this.delta))
						* Math.pow(Math.log(1.0 - Math.exp(-this.dStar / this.delta)), 2.0)
				- this.H1 * this.H1;
		return true;
	}

//+------------------------------------------------------------------+
//|   "lite" version of the estimator with fewer fields, used only   |
//|   if event is not already provided otherwise.                    |
//+------------------------------------------------------------------+
	private int EstimateEvent(double price_bid, double price_ask, iMeasure measureFunc) {

		if (!this.initalized) {
			this.marketMode = -1;
			this.initalized = true;
			this.extreme = this.reference = (price_bid + price_ask) / 2.0;
			return 0;
		}

		if (this.marketMode == -1) {
			if (measureFunc.Change(price_bid, this.extreme) >= this.deltaUp) {
				this.marketMode = 1;
				this.extreme = price_ask;
				this.reference = price_ask;
				return 1;
			}
			if (price_ask < this.extreme) {
				this.extreme = price_ask;
			}
			if (measureFunc.Change(this.reference, this.extreme) >= this.dStar) {
				this.reference = this.extreme;
				return 2;
			}
		} else if (this.marketMode == 1) {
			if (measureFunc.Change(price_ask, this.extreme) <= -this.deltaDown) {
				this.marketMode = -1;
				this.extreme = price_bid;
				this.reference = price_bid;
				return -1;
			}
			if (price_bid > this.extreme) {
				this.extreme = price_bid;
			}
			if (measureFunc.Change(this.reference, this.extreme) <= -this.dStar) {
				this.reference = this.extreme;
				return -2;
			}
		}
		return 0;
	}

//+------------------------------------------------------------------+
//|   Calcualte liquidity with provided event. If liquidity is  used |
//|   with unoptimized delta* to delta ratio, the exact version      |
//|   should ne used.                                                |
//+------------------------------------------------------------------+
	public boolean ComputeLiquidityExternal(int event) {
		if (event != 0) {
			this.surp = this.alphaWeight * (Math.abs(event) == 1 ? 0.08338161 : 2.525729)
					+ (1.0 - this.alphaWeight) * this.surp;

			// --- exact version
			// surp = alphaWeight*(MathAbs(event) == 1 ? surpDC : surpOS) + (1.0 -
			// alphaWeight)*surp;

			if (event > 0) // down moves
			{
				this.downSurp = this.alphaWeight * (event == 1 ? 0.08338161 : 2.525729)
						+ (1.0 - this.alphaWeight) * this.downSurp;

				// --- exact version
				// downSurp = alphaWeight*(event == 1 ? surpDC : surpOS) + (1.0 -
				// alphaWeight)*downSurp;
			} else if (event < 0) // up moves
			{
				this.upSurp = this.alphaWeight * (event == -1 ? 0.08338161 : 2.525729)
						+ (1.0 - this.alphaWeight) * this.upSurp;

				// --- exact version
				// upSurp = alphaWeight*(event == -1 ? surpDC : surpOS) + (1.0 -
				// alphaWeight)*upSurp;
			}

			// --- we include also up and down liquidity, they only account for events
			// pointing to the respective direction
			this.liquidity = 1.0 - CumNorm(Math.sqrt(this.alpha) * (this.surp - this.H1) / Math.sqrt(this.H2));
			this.liquidityUp = 1.0 - CumNorm(Math.sqrt(this.alpha) * (this.upSurp - this.H1) / Math.sqrt(this.H2));
			this.liquidityDown = 1.0 - CumNorm(Math.sqrt(this.alpha) * (this.downSurp - this.H1) / Math.sqrt(this.H2));
		}

		return true;
	}
//+------------------------------------------------------------------+

//+------------------------------------------------------------------+
//|   Calulate event in the class, then calculate the liquidity.     |
//|   If liquidity is  used with unoptimized delta* to delta ratio,  |
//|   the exact version should ne used.                              |
//+------------------------------------------------------------------+
//--- note that we use directly the optimized values (see notes) for the surprise
//--- calculatiuons. Fllowing the formulas in the notes, one can easily use arbitrary deltas
//--- for that calculation. For this, the comment net to be switched to the lines including surpOS and surpDC

	public boolean ComputeLiquidityInternal(QuoteDataFeed quoteDataFeed, iMeasure measureFunc) {

		int event = EstimateEvent(quoteDataFeed.GetQuote("FX", this.underlying).GetBid(),
				quoteDataFeed.GetQuote("FX", underlying).GetAsk(), measureFunc);
		if (event != 0) {
			this.surp = this.alphaWeight * (Math.abs(event) == 1 ? 0.08338161 : 2.525729)
					+ (1.0 - this.alphaWeight) * this.surp;

			// --- exact version
			// surp = alphaWeight*(MathAbs(event) == 1 ? surpDC : surpOS) + (1.0 -
			// alphaWeight)*surp;

			if (event > 0) // down moves
			{
				this.downSurp = this.alphaWeight * (event == 1 ? 0.08338161 : 2.525729)
						+ (1.0 - this.alphaWeight) * this.downSurp;

				// --- exact version
				// downSurp = alphaWeight*(event == 1 ? surpDC : surpOS) + (1.0 -
				// alphaWeight)*downSurp;
			} else if (event < 0) // up moves
			{
				this.upSurp = this.alphaWeight * (event == -1 ? 0.08338161 : 2.525729)
						+ (1.0 - this.alphaWeight) * this.upSurp;

				// --- exact version
				// upSurp = alphaWeight*(event == -1 ? surpDC : surpOS) + (1.0 -
				// alphaWeight)*upSurp;
			}

			// --- we include also up and down liquidity, they only account for events
			// pointing to the respective direction
			this.liquidity = 1.0 - CumNorm(Math.sqrt(this.alpha) * (this.surp - this.H1) / Math.sqrt(this.H2));
			this.liquidityUp = 1.0 - CumNorm(Math.sqrt(this.alpha) * (this.upSurp - this.H1) / Math.sqrt(this.H2));
			this.liquidityDown = 1.0 - CumNorm(Math.sqrt(this.alpha) * (this.downSurp - this.H1) / Math.sqrt(this.H2));
		}
		return true;
	}

//+------------------------------------------------------------------+
//|   implementation of the CNDF for a standard normal: N(0,1)       |
//+------------------------------------------------------------------+
	public static double CumNorm(double x) {
//protect against overflow
		if (x > 6.0)
			return 1.0;
		if (x < -6.0)
			return 0.0;

		double b1 = 0.31938153;
		double b2 = -0.356563782;
		double b3 = 1.781477937;
		double b4 = -1.821255978;
		double b5 = 1.330274429;
		double p = 0.2316419;
		double c2 = 0.3989423;

		double a = Math.abs(x);
		double t = 1.0 / (1.0 + a * p);
		double b = c2 * Math.exp((-x) * (x / 2.0));
		double n = ((((b5 * t + b4) * t + b3) * t + b2) * t + b1) * t;
		n = 1.0 - b * n;

		if (x < 0.0)
			n = 1.0 - n;

		return n;
	}
}
//+------------------------------------------------------------------+

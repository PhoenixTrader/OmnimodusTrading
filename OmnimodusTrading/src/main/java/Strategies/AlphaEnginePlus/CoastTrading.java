package Strategies.AlphaEnginePlus;

import DataFeed.QuoteDataFeed.*;
import Infrastructure.OrderManagement.*;
import Strategies.Measures.*;
import Infrastructure.Order.*;

import java.util.*;
import java.util.ArrayList;

public class CoastTrading {
	// +------------------------------------------------------------------+
	// | The class that shall execute the asymmetric coastline trading. |
	// +------------------------------------------------------------------+

	private String underlying;
	private iMeasure measureFunc;
	private double fractionLong;
	private double fractionShort;
	// --- events
	private EventEstimator eEstimator; // --- event estimator objects to estimate the event that occured
	private boolean estimatorStatus;
	private ArrayList<EventEstimator> eEstimatorDown; // --- skewed event estimators, have to be updated separately
	private ArrayList<Boolean> estimatorDownStatus;
	private ArrayList<EventEstimator> eEstimatorUp; // --- skewed event estimators, have to be updated separately
	private ArrayList<Boolean> estimatorUpStatus;
	private int eventLong; // --- the current event, the return value of the event estimator function
	private int eventShort;
	private LocalLiquidity localLiquidity; // local liquidity, updated in each step
	private double shrinkFlong, shrinkFshort; // --- decrease or increase factor for the position size
	private double profitBasketLong;
	private double profitBasketShort;
	private double averagePriceLong;
	private double averagePriceShort;
	// ======================== Profit Taking Scale factos
	// =============================
	private double factorProfitLong;
	private double factorProfitShort;
	// ====================== Position Control Fields =======================
	private double positionCounterShort;
	private double positionCounterLong;
	private double positionLong;
	private double positionShort;
	private int counter;
	private int identifier;
	private CoastTradingSetting setting;

	// ============================================ Constructors
	// ==============================================================
	public CoastTrading() {
	};

	// ======================================= Get Functions
	// =================================
	double GetPositionCount(Boolean longPosition) {
		return longPosition ? positionCounterLong : positionCounterShort;
	}

	double GetTotalPosition() {
		return positionLong - positionShort;
	}

	double GetLongPosition() {
		return positionLong;
	}

	double GetShortPosition() {
		return positionShort;
	}

	int GetMagic() {
		return setting.Index();
	}

	int GetEvent(int ls) {
		if (ls == 1)
			return eventLong;
		else if (ls == -1)
			return eventShort;
		return 0;
	}

	double GetProfitBasket() {
		return profitBasketLong + profitBasketShort;
	}

	double GetAveragePrice(Boolean longPosition) {
		if (longPosition)
			return averagePriceLong;
		else
			return averagePriceShort;
	}

	// ======================================= Misc
	// ======================================================

	// +------------------------------------------------------------------+
	// | The constructor that copy-pastes the input parameters into |
	// | fields values. |
	// +------------------------------------------------------------------+
	public CoastTrading(String rate, 
			double dOriginal, double dUp, double dDown, double dStarUp, double dStarDown,
			int id , double posSize, String measurement, 
			ExposureManagement exposureManagement, QuoteDataFeed quoteDataFeed) {

		InitializeMeasure(measurement);
		this.underlying = rate;
		// --- set barrier levels based on order sizes
		ArrayList<Double> barrierLevels = DefineBarrierLevels( posSize);

		// --- definition of the coast trading settings
		this.setting = new CoastTradingSetting(dUp, dDown, dOriginal, posSize, barrierLevels, id);
		this.counter = 0;
		// --- initialises most fields that are numbers;
		// --- Important: the average price is also calculated in that method,
		// --- that is required if the algorithm undergoes a parameter change during
		// live trading
		InitializeFields();

		// --- event status booleans - these tell where we calculate the events with
		this.estimatorStatus = true;
		this.estimatorDownStatus = new ArrayList<Boolean>(Arrays.asList(false, false));
		this.estimatorUpStatus = new ArrayList<Boolean>(Arrays.asList(false, false));

		this.eEstimator = new EventEstimator(this.underlying, dUp, dDown, dStarUp, dStarDown, quoteDataFeed);
		// --- event estimators for skewed positions
		//eEstimatorDown = new ArrayList<EventEstimator>(Arrays.asList(null, null));
		//eEstimatorUp = new ArrayList<EventEstimator>(Arrays.asList(null, null));
		
		eEstimatorDown = new ArrayList<EventEstimator>(Arrays.asList(new EventEstimator(), new EventEstimator()));
		eEstimatorUp = new ArrayList<EventEstimator>(Arrays.asList(new EventEstimator(), new EventEstimator()));
		
		this.eEstimatorDown.set(0, new EventEstimator(this.underlying, 0.75 * dUp, 1.50 * dDown, 0.75 * dStarUp,
				0.75 * dStarUp, quoteDataFeed));
		this.eEstimatorDown.set(1, new EventEstimator(this.underlying, 0.50 * dUp, 2.00 * dDown, 0.50 * dStarUp,
				0.50 * dStarUp, quoteDataFeed));
		this.eEstimatorUp.set(0, new EventEstimator(this.underlying, 1.50 * dUp, 0.75 * dDown, 0.75 * dStarDown,
				0.75 * dStarDown, quoteDataFeed));
		this.eEstimatorUp.set(1, new EventEstimator(this.underlying, 2.00 * dUp, 0.50 * dDown, 0.50 * dStarDown,
				0.50 * dStarDown, quoteDataFeed));

		this.localLiquidity = new LocalLiquidity(this.underlying, dOriginal, dUp, dDown, dOriginal * 2.525729, 50.0,
				quoteDataFeed);

		int lookbackFrame = 3 * 8640; // --- 1 month lookback if 5 min selected
		int timeframe = 5;
		double spread = 0.00015;
		this.identifier = id;
		/*
		 * if(eEstimator.InitializeTracker(spread, timeframe, lookbackFrame,
		 * measureFunc, localLiquidity)) System.out.print("Trading pair " +
		 * IntegerToString(setting.Index()) + " initial mode: " +
		 * IntegerToString(eEstimator.GetMarketMode()) + " | initial linquidity value: "
		 * + Double.toString(localLiquidity.GetLiquidity()));
		 * 
		 * eEstimatorDown[0].InitializeTrackerRaw(spread, timeframe, lookbackFrame,
		 * measureFunc); eEstimatorDown[1].InitializeTrackerRaw(spread, timeframe,
		 * lookbackFrame, measureFunc); eEstimatorUp[0].InitializeTrackerRaw(spread,
		 * timeframe, lookbackFrame, measureFunc);
		 * eEstimatorUp[1].InitializeTrackerRaw(spread, timeframe, lookbackFrame,
		 * measureFunc);
		 * 
		 * exposureManagement.GetPositions(setting.Index() - 1);
		 */
	}

	// +------------------------------------------------------------------+
	// | Definition of the trading strategy, includes long and short |
	// | trading for efficiacy reasons as less variables have to be |
	// | stored. |
	// +------------------------------------------------------------------+
	public Map<String, ArrayList<Order>> TradeAsymmetric(QuoteDataFeed quoteDataFeed,
			ExposureManagement exposureManagement,
			OrderManagement orderManagement/* , GlobalLiquidity &globalLiquidity */) {
		
		ArrayList<Order> orders = new ArrayList<Order>();
		double exposureClean = exposureManagement.GetCleanExposure(setting.Index() - 1);
		// --- calculate the event
		eventLong = eventShort = eEstimator.EstimateEvent(quoteDataFeed, measureFunc);

		//eEstimator.Print();

		// --- calculate the local liquidity
		if (!localLiquidity.ComputeLiquidityExternal(eventLong))
			System.out.print("Didn't compute liquidity!\n");

		// --- ================================ Long trading
		// ===============================================================
		quoteDataFeed.Refresh();
		SkewEventLong(quoteDataFeed);

		double sizeLong = PositionSizeLong(); // --- determine the position sizing based on local and global liquidity

		if (eventLong < 0) {

			PositionAdjustLong(sizeLong, exposureManagement);
			RiskAppetiteAdjustLong(exposureManagement);

			if (positionLong == 0.0) {
				double sizeToAdd = CheckOrderSize(sizeLong);

				Order order = new Order( "OPEN", "BUY", sizeToAdd, "FX", this.underlying, 
			             quoteDataFeed, this.counter, this.identifier, "comment"); /*
											 * OrderSend(fxRate, OP_BUY, sizeToAdd, Ask, slippage, NO stop loss 0, take
											 * profit 0, NULL, setting.Index() magic, to keep the coast trader orders
											 * separate);
											 */
				this.counter += 1;
				orders.add(order);
				
				System.out.print("Coast strategy long " + Integer.toString(setting.Index()) + " open long position of "
						+ Double.toString(NormalizeDouble(sizeToAdd, 2)) + " lots\n");
				positionCounterLong += 1.0;
				positionLong += sizeToAdd;
				averagePriceLong = quoteDataFeed.GetFXQuote(this.underlying).GetAsk();
				exposureManagement.UpdatePositionLong(setting.Index() - 1, positionLong);

			} // end if for opening first long position
			else if (positionLong > 0.0) {
				// Increase long position (buy)

				double sizeToAdd = CheckOrderSize(sizeLong * fractionLong * shrinkFlong);

				Order order = new Order( "OPEN", "BUY", sizeToAdd, "FX", this.underlying, 
			             quoteDataFeed, this.counter, this.identifier, "comment") ; /*
											 * OrderSend(fxRate, OP_BUY, sizeToAdd, Ask, slippage, NO stop loss 0, take
											 * profit 0, NULL, setting.Index() magic, to keep the coast trader orders
											 * separate);
											 */
				this.counter += 1;
				orders.add(order);
						
				System.out.print("Coast strategy long " + Integer.toString(setting.Index()) + ": cascade with "
						+ Double.toString(NormalizeDouble(sizeToAdd, 2)) + " lots\n");
				// UpdateAverageOpen(sizeToAdd, true);
				exposureManagement.UpdatePositionLong(setting.Index() - 1, positionLong);

			} // end if long positions exist and increased
		} // end if for event < 0
		else // --- decascading, closing long positions
		if (eventLong > 0 && positionLong > 0.0
				&& localLiquidity.GetLiquidity() < exposureManagement.GetLiquidityBarrier()
				&& exposureManagement.CheckBarrierLevelLong(4)) {

			for (Order order_active : orderManagement.GetOrdersActive("FX").get(this.underlying)) // <-- for loop to
																									// loop through all
																									// Orders . . COUNT
																									// DOWN TO ZERO !
			{
				if (order_active.GetIdentifier2() == setting.Index() // --- magic number match, for the order with
																		// respect to the correct delta
						&& order_active.GetType() == "BUY") // --- match buy orders with the long strategy
				{
					if (measureFunc.Change(quoteDataFeed.GetFXQuote(this.underlying).GetBid(),
							order_active.GetOpenReferencePrice()) >= setting.GetDeltaUpLong()) // --- condition for
																								// decascade
					{
						// profitBasketLong += OrderProfit() > 0.0 ? OrderProfit() : 0.0;
						// double lots = OrderLots();
						// double price = OrderOpenPrice();
						Order order = Order.Close(order_active, quoteDataFeed, "comment");
						this.counter += 1;
						orders.add(order);
						
						System.out.print("Coast strategy long " + Integer.toString(setting.Index()) + " descascade: "
								+ Double.toString(order_active.GetPnL()) + "\n");
						// UpdateAverageClose(lots, price, true);
						exposureManagement.UpdatePositionLong(setting.Index() - 1, positionLong);

					} // end if for decascade condition
				} // end if for order selection
			} // end for loop through open orders

		} // end if for event > 0 && totalPosition > 0.0

		// ============================================ Short Trading
		// ===========================================0

		double sizeShort = PositionSizeShort(); // --- determine the position sizing based on local and global liquidity

		quoteDataFeed.Refresh();
		SkewEventShort(quoteDataFeed);

		if (eventShort > 0) {

			PositionAdjustShort(sizeShort, exposureManagement);
			RiskAppetiteAdjustShort(exposureManagement);

			if (positionShort == 0.0) {
				double sizeToAdd = CheckOrderSize(sizeShort);

				Order order = new Order( "OPEN", "SELL", sizeToAdd, "FX", this.underlying, 
			             quoteDataFeed, this.counter, this.identifier, "comment"); /*
											 * int ticket = OrderSend(fxRate, OP_SELL, sizeToAdd, Bid, slippage, NO stop
											 * loss 0, take profit 0, NULL, -setting.Index() magic, to keep the coast
											 * trader orders separate);
											 */
				this.counter += 1;
				orders.add(order);
				System.out.print("Coast strategy short " + Integer.toString(setting.Index())
						+ ": open short position of " + Double.toString(NormalizeDouble(sizeToAdd, 2)) + " lots\n");

				exposureManagement.UpdatePositionShort(setting.Index() - 1, positionShort);

			} // end if for opening first short position
			else if (positionShort > 0.0) {
				double sizeToAdd = CheckOrderSize(sizeShort * fractionShort * shrinkFshort);

				Order order = new Order( "OPEN", "SELL", sizeToAdd, "FX", this.underlying, 
			             quoteDataFeed, this.counter, this.identifier, "comment"); /*
											 * int ticket = OrderSend(fxRate, OP_SELL, sizeToAdd, Bid, slippage, NO stop
											 * loss 0, take profit 0, NULL, -setting.Index() magic, to keep the coast
											 * trader orders separate);
											 */
				this.counter += 1;
				orders.add(order);
				System.out.print("Coast strategy short " + Integer.toString(setting.Index()) + " cascade with "
						+ Double.toString(NormalizeDouble(sizeToAdd, 2)) + " lots\n");

				exposureManagement.UpdatePositionShort(setting.Index() - 1, positionShort);

			} // end if for opening additional short position
		} // end if for event > 0
		else if (eventShort < 0 && positionShort > 0.0
				&& localLiquidity.GetLiquidity() < exposureManagement.GetLiquidityBarrier()
				&& exposureManagement.CheckBarrierLevelShort(4)) {

			for (Order order_active : orderManagement.GetOrdersActive("FX").get(this.underlying)) // <-- for loop to
																									// loop through all
																									// Orders . . COUNT
																									// DOWN TO ZERO !
			{

				if (order_active.GetIdentifier2() == -setting.Index() // --- magic number match
						&& order_active.GetType() == "SELL") // --- match sell orders with the short strategy
				{
					if (measureFunc.Change(quoteDataFeed.GetFXQuote(this.underlying).GetAsk(),
							order_active.GetOpenReferencePrice()) <= -setting.GetDeltaDownShort()) // condition for
																									// decascade
					{

						Order order = Order.Close(order_active, quoteDataFeed, "comment");
						this.counter += 1;
						orders.add(order);
						System.out.print("Coast strategy short" + Integer.toString(setting.Index()) + " descascade: "
								+ Double.toString(order_active.GetPnL())+"\n");
						// UpdateAverageClose(lots, price, false);
						exposureManagement.UpdatePositionShort(setting.Index() - 1, positionShort);

					} // end if for overshoot
				} // end if order selection
			} // end for loop through orders
		} // end if for event < 0 with open short position

		// exposureLevels[setting.Index() - 1] = GetTotalPosition();
		
		//orders.add(new Order());

		return Map.of(this.underlying, orders);

	}

	// +------------------------------------------------------------------+
	// | The function that shall take profit if the average price |
	// | satisfies the overshoot (take-profit) condition |
	// +------------------------------------------------------------------+
	public Map<String, ArrayList<Order>> TakeProfit(QuoteDataFeed quoteDataFeed, ExposureManagement exposureManagement,
			OrderManagement orderManagement /* , GlobalLiquidity &globalLiquidity */) {
		// System.out.print("Pair " + IntegerToString(setting.Index()) + " pos long: " +
		// Double.toString(positionLong) + " | pos short: " +
		// Double.toString(positionShort));
		quoteDataFeed.Refresh();

		RiskAppetiteAdjustLong(exposureManagement);
		if (ProfitCondition(true, quoteDataFeed)) {
			double exposureClean = exposureManagement.GetCleanExposure(setting.Index() - 1); // --- get net exposure
																								// excluding this
																								// trading pair
			if (localLiquidity.GetLiquidity() >= 0.5 /* globalLiquidity.GetLiquidity() >= 0.5 */) {
				CloseAllOrders(true, quoteDataFeed, orderManagement);
				if (exposureClean - positionShort < 0.0) // --- if the net exposure is negative, we close the worst
															// performing short position
				{
					if (exposureManagement.CheckBarriers(-(exposureClean - positionShort), 0, 2))
						return CloseBestNWorst(false, 1, 2, orderManagement, quoteDataFeed);
					else if (exposureManagement.CheckBarriers(-(exposureClean - positionShort), 2, 4))
						return CloseBestNWorst(false, 2, 3, orderManagement, quoteDataFeed);
					else if (exposureManagement.CheckBarrierSingle(-(exposureClean - positionShort), 4))
						return CloseBestNWorst(false, 3, 4, orderManagement, quoteDataFeed);
					else
						return CloseWorst(false, orderManagement, quoteDataFeed);
				}

				factorProfitLong = 0.667;
			} else {
				if (exposureManagement.CheckBarriers(exposureClean, 0, 3))
					return CloseBestNWorst(true, 2, 1, orderManagement, quoteDataFeed);
				else if (exposureManagement.CheckBarrierSingle(exposureClean, 3))
					return CloseBestNWorst(true, 3, 2, orderManagement, quoteDataFeed);
				else
					return CloseBest(true, orderManagement, quoteDataFeed); // CloseBestNWorst(true, 1,1);//
																			// CloseBest(true);
			}
			exposureManagement.UpdatePosition(setting.Index() - 1, positionLong, positionShort);
		} // --- end profit condition long

		RiskAppetiteAdjustShort(exposureManagement);
		if (ProfitCondition(false, quoteDataFeed)) {
			double exposureClean = exposureManagement.GetCleanExposure(setting.Index() - 1); // --- get net exposure
																								// excluding this
																								// trading pair
			if (localLiquidity.GetLiquidity() >= 0.5 /* globalLiquidity.GetLiquidity() >= 0.5 */) {
				CloseAllOrders(false, quoteDataFeed, orderManagement);
				if (exposureClean + positionLong > 0.0) // --- if the net exposure is positive, we close the worst
														// performing long position
				{
					if (exposureManagement.CheckBarriers(exposureClean + positionLong, 0, 2))
						return CloseBestNWorst(true, 1, 2, orderManagement, quoteDataFeed);
					else if (exposureManagement.CheckBarriers(exposureClean + positionLong, 2, 4))
						return CloseBestNWorst(true, 2, 3, orderManagement, quoteDataFeed);
					else if (exposureManagement.CheckBarrierSingle(exposureClean + positionLong, 4))
						return CloseBestNWorst(true, 3, 4, orderManagement, quoteDataFeed);
					else
						return CloseWorst(true, orderManagement, quoteDataFeed);
				}
				factorProfitShort = 0.667;

			} else {
				if (exposureManagement.CheckBarriers(-exposureClean, 0, 3))
					return CloseBestNWorst(false, 2, 1, orderManagement, quoteDataFeed);
				else if (exposureManagement.CheckBarrierSingle(-exposureClean, 3))
					CloseBestNWorst(false, 3, 2, orderManagement, quoteDataFeed);
				else
					return CloseBest(false, orderManagement, quoteDataFeed); // CloseBestNWorst(false, 1,1);//
																				// CloseBest(false);
			}
			exposureManagement.UpdatePosition(setting.Index() - 1, positionLong, positionShort); // --- update the net
																									// exposure
		} // --- end profit condition short
		
		return Map.of(this.underlying, new ArrayList<Order>());
	}

	// ==========================================================================================================

	// +------------------------------------------------------------------+
	// | Calculte the position-size for short positions based |
	// | on liquidity. |
	// +------------------------------------------------------------------+
	double PositionSizeShort() {
		double size = 0.0;
		size = localLiquidity.GetLiquidity() < 0.5 ? 0.5 : 1.0;
		size = localLiquidity.GetLiquidity() < 0.1 ? 0.1 : size;

		return size * setting.GetSizeShort();
	}

	// +------------------------------------------------------------------+
	// | Calculte the position-size for long positions based |
	// | on liquidity. |
	// +------------------------------------------------------------------+
	double PositionSizeLong() {
		double size = 0.0;
		size = localLiquidity.GetLiquidity() < 0.5 ? 0.5 : 1.0;
		size = localLiquidity.GetLiquidity() < 0.1 ? 0.1 : size;

		return size * setting.GetSizeShort();
	}

	// +------------------------------------------------------------------+

	// +------------------------------------------------------------------+
	// | In case the short-position lies between or above the |
	// | above the thresholds, we use the skewed EventEstimators to |
	// | provide the event key for thre long position. |
	// +------------------------------------------------------------------+
	void SkewEventLong(QuoteDataFeed quoteDataFeed) {

		if (positionLong >= setting.GetBarrierLong(0) && positionLong < setting.GetBarrierLong(1)) {
			eventLong = eEstimatorDown.get(0).EstimateEvent(quoteDataFeed, measureFunc);
			if (!estimatorDownStatus.get(0)) {
				estimatorDownStatus.set(0, true);
				estimatorStatus = false;
				estimatorDownStatus.set(1, false);
			}

			eEstimatorDown.get(1).EstimateEvent(quoteDataFeed, measureFunc);
			fractionLong = 0.5;
		} else if (positionLong >= setting.GetBarrierLong(1)) {
			eventLong = eEstimatorDown.get(1).EstimateEvent(quoteDataFeed, measureFunc);
			if (!estimatorDownStatus.get(1)) {
				estimatorDownStatus.set(1, true);
				estimatorStatus = false;
				estimatorDownStatus.set(0, false);
			}

			eEstimatorDown.get(0).EstimateEvent(quoteDataFeed, measureFunc);
			fractionLong = 0.25;
		} else {
			eEstimatorDown.get(0).EstimateEvent(quoteDataFeed, measureFunc);
			eEstimatorDown.get(1).EstimateEvent(quoteDataFeed, measureFunc);
			if (!estimatorStatus) {
				estimatorStatus = true;
				estimatorDownStatus.set(1, false);
				estimatorDownStatus.set(0, false);
			}

		}
	}

	// +------------------------------------------------------------------+
	// | In case the long-position lies between or above the |
	// | above the thresholds, we use the skewed EventEstimators to |
	// | provide the event key for thre short position. |
	// +------------------------------------------------------------------+
	void SkewEventShort(QuoteDataFeed quoteDataFeed) {
		if (positionShort >= setting.GetBarrierShort(0) && positionShort < setting.GetBarrierShort(1)) {
			eventShort = eEstimatorUp.get(0).EstimateEvent(quoteDataFeed, measureFunc);
			if (!estimatorUpStatus.get(0)) {
				estimatorUpStatus.set(0, true);
				estimatorStatus = false;
				estimatorUpStatus.set(1, false);
			}
			eEstimatorUp.get(1).EstimateEvent(quoteDataFeed, measureFunc);
			fractionShort = 0.5;
		} else if (positionShort >= setting.GetBarrierShort(1)) {
			eventLong = eEstimatorUp.get(1).EstimateEvent(quoteDataFeed, measureFunc);
			if (!estimatorUpStatus.get(1)) {
				estimatorUpStatus.set(1, true);
				estimatorStatus = false;
				estimatorUpStatus.set(0, false);

			}
			eEstimatorUp.get(0).EstimateEvent(quoteDataFeed, measureFunc);
			fractionShort = 0.25;
		} else {
			eEstimatorUp.get(0).EstimateEvent(quoteDataFeed, measureFunc);
			eEstimatorUp.get(1).EstimateEvent(quoteDataFeed, measureFunc);
			if (!estimatorStatus) {
				estimatorStatus = true;
				estimatorUpStatus.set(0, false);
				estimatorUpStatus.set(1, false);
			}
		}

	}
	// +------------------------------------------------------------------+

	// +------------------------------------------------------------------+
	// | |
	// +------------------------------------------------------------------+
	void RiskAppetiteAdjustLong(ExposureManagement exposureManagement) {

		if ((positionShort > setting.GetBarrierShort(0) && positionShort <= setting.GetBarrierShort(1))) {
			factorProfitLong = 1.0;
			factorProfitShort = 0.5;
		} else if (positionShort > setting.GetBarrierShort(1) && positionShort <= setting.GetBarrierShort(2)) {
			factorProfitLong = 1.5;
			factorProfitShort = 0.25;
		} else if (exposureManagement.CheckBarrierLevelShort(2) /* positionShort > setting.GetBarrierShort(2) */) {
			factorProfitLong = 2.0;
			factorProfitShort = 0.05;
		} else
			factorProfitLong = 0.667;
	}

	// +------------------------------------------------------------------+
	// | |
	// +------------------------------------------------------------------+
	void RiskAppetiteAdjustShort(ExposureManagement exposureManagement) {
		if ((positionLong > setting.GetBarrierLong(0) && positionLong <= setting.GetBarrierLong(1))) {
			factorProfitShort = 1.0;
			factorProfitLong = 0.5;
		} else if (positionLong > setting.GetBarrierLong(1) && positionLong <= setting.GetBarrierLong(2)) {
			factorProfitShort = 1.5;
			factorProfitLong = 0.25;
		} else if (exposureManagement.CheckBarrierLevelLong(2) /* positionLong > setting.GetBarrierLong(2) */) {
			factorProfitShort = 2.0;
			factorProfitLong = 0.05;
		} else
			factorProfitShort = 0.667;
	}

	// +------------------------------------------------------------------+
	// | |
	// +------------------------------------------------------------------+
	void PositionAdjustLong(double sizeLong, ExposureManagement exposureManagement) {
		if (exposureManagement.GetNetExposure() <= 0.0) {
			if (exposureManagement.CheckBarrierBetweenShort(0, 1))
				sizeLong *= 1.1;
			else if (exposureManagement.CheckBarrierBetweenShort(1, 2))
				sizeLong *= 1.2;
			else if (exposureManagement.CheckBarrierBetweenShort(2, 3))
				sizeLong *= 1.3;
			else if (exposureManagement.CheckBarrierBetweenShort(3, 4))
				sizeLong *= 1.4;
			else if (exposureManagement.CheckBarrierLevelShort(4))
				sizeLong *= 1.5;
		} else {
			if (exposureManagement.CheckBarrierBetweenLong(0, 1))
				sizeLong *= 0.9;
			else if (exposureManagement.CheckBarrierBetweenLong(1, 2))
				sizeLong *= 0.8;
			else if (exposureManagement.CheckBarrierBetweenLong(2, 3))
				sizeLong *= 0.7;
			else if (exposureManagement.CheckBarrierBetweenLong(3, 4))
				sizeLong *= 0.6;
			else if (exposureManagement.CheckBarrierLevelLong(4))
				sizeLong *= 0.5;
		}
	}

	// +------------------------------------------------------------------+
	// | |
	// +------------------------------------------------------------------+
	void PositionAdjustShort(double sizeShort, ExposureManagement exposureManagement) {
		if (exposureManagement.GetNetExposure() >= 0.0) {
			if (exposureManagement.CheckBarrierBetweenLong(0, 1))
				sizeShort *= 1.1;
			else if (exposureManagement.CheckBarrierBetweenLong(1, 2))
				sizeShort *= 1.2;
			else if (exposureManagement.CheckBarrierBetweenLong(2, 3))
				sizeShort *= 1.3;
			else if (exposureManagement.CheckBarrierBetweenLong(3, 4))
				sizeShort *= 1.4;
			else if (exposureManagement.CheckBarrierLevelLong(4))
				sizeShort *= 1.5;
		} else {
			if (exposureManagement.CheckBarrierBetweenShort(0, 1))
				sizeShort *= 0.9;
			else if (exposureManagement.CheckBarrierBetweenShort(1, 2))
				sizeShort *= 0.8;
			else if (exposureManagement.CheckBarrierBetweenShort(2, 3))
				sizeShort *= 0.7;
			else if (exposureManagement.CheckBarrierBetweenShort(3, 4))
				sizeShort *= 0.6;
			else if (exposureManagement.CheckBarrierLevelShort(4))
				sizeShort *= 0.5;

		}
	}

	// +------------------------------------------------------------------+
	// | Function to update the average price if a position is opened. |
	// +------------------------------------------------------------------+
	void UpdateAverageOpen(double lots, Boolean longPosition, QuoteDataFeed quoteDataFeed) {
		if (longPosition) {
			double posOld = positionLong;
			positionLong += lots;
			positionCounterLong += 1;
			averagePriceLong = (posOld * averagePriceLong + quoteDataFeed.GetFXQuote(this.underlying).GetAsk() * lots)
					/ positionLong;
		} else {
			double posOld = positionShort;
			positionShort += lots;
			positionCounterShort += 1;
			averagePriceShort = (posOld * averagePriceShort + quoteDataFeed.GetFXQuote(this.underlying).GetBid() * lots)
					/ positionShort;
		}
	}

	// +------------------------------------------------------------------+
	// | Function that updates the average price in the case |
	// | that a position is closed. |
	// +------------------------------------------------------------------+
	void UpdateAverageClose(double lots, double price, Boolean longPosition) {
		if (longPosition) {
			double posOld = positionLong;
			positionLong -= lots;
			positionCounterLong -= 1;
			averagePriceLong = positionLong < 1e-5 ? 0.0 : (posOld * averagePriceLong - price * lots) / positionLong;
		} else {
			double posOld = positionShort;
			positionShort -= lots;
			positionCounterShort -= 1;
			averagePriceShort = positionShort < 1e-5 ? 0.0
					: (posOld * averagePriceShort - price * lots) / positionShort;
		}
	}
	// +------------------------------------------------------------------+

	// +------------------------------------------------------------------+
	// | The function that returns the long profit condition if input |
	// | is true, otherwise the short condition is provided. |
	// +------------------------------------------------------------------+
	Boolean ProfitCondition(Boolean longPosition, QuoteDataFeed quoteDataFeed) {
		if (longPosition)
			return positionLong > 1e-5 && averagePriceLong > 1e-5
					&& measureFunc.Change(quoteDataFeed.GetFXQuote(this.underlying).GetBid(),
							averagePriceLong) >= setting.GetDeltaUpLong() * factorProfitLong;
		else
			return positionShort > 1e-5 && averagePriceShort > 1e-5
					&& measureFunc.Change(quoteDataFeed.GetFXQuote(this.underlying).GetAsk(),
							averagePriceShort) <= -setting.GetDeltaDownShort() * factorProfitShort;
	}

	// +------------------------------------------------------------------+
	// | If input is true, all long positions corresponding to this |
	// | pair are closed, otherwise the short side will be closed. |
	// +------------------------------------------------------------------+
	Map<String, ArrayList<Order>> CloseAllOrders(Boolean longPosition, QuoteDataFeed quoteDataFeed,
			OrderManagement orderManagement) {
		ArrayList<Order> orders = new ArrayList<Order>();

		if (longPosition && positionLong > 1e-5 && averagePriceLong > 1e-5) {
			System.out.print("Coast strategy long " + Integer.toString(setting.Index())
					+ " to close all positions at average pice " + Double.toString(averagePriceLong) + ".\n");

			// int totalNumberOfOrders = OrdersTotal(); // <-- we store the number of Orders
			// in the variable

			for (Order order_active : orderManagement.GetOrdersActive("FX").get(this.underlying)) // <-- for loop to
																									// loop through all
			{

				if (order_active.GetIdentifier2() == setting.Index() // --- magic number match
						&& order_active.GetType() == "BUY") // --- match sell orders with the short strategy
				{

					Order order = Order.Close(order_active, quoteDataFeed, "comment");
					orders.add(order);
					/*
					 * if (!OrderClose(OrderTicket(), OrderLots(), Bid, slippage)) // <-- try to
					 * close the order System.out.print("Order Close failed, order number: ",
					 * OrderTicket(), " Error: ", GetLastError()); // <-- if the Order Close failed
					 * print some helpful information
					 */

				}
			}
			/*
			 * this.positionLong = 0.0; positionCounterLong = 0.0; averagePriceLong = 0.0;
			 */
		} else if (positionShort > 1e-5 && averagePriceShort > 1e-5) {
			System.out.print("Coast strategy short " + Integer.toString(setting.Index())
					+ " to close all positions at average pice " + Double.toString(averagePriceShort) + ".\n");

			// int totalNumberOfOrders = OrdersTotal(); // <-- we store the number of Orders
			// in the variable

			for (Order order_active : orderManagement.GetOrdersActive("FX").get(this.underlying)) // <-- for loop to
																									// loop through all
																									// Orders . . COUNT
																									// DOWN TO ZERO !
			{

				if (order_active.GetIdentifier2() == setting.Index() // --- magic number match
						&& order_active.GetType() == "SELL") // --- match sell orders with the short strategy
				{

					Order order = Order.Close(order_active, quoteDataFeed, "comment");
					orders.add(order);
					/*
					 * if (!OrderClose(OrderTicket(), OrderLots(), Ask, slippage)) // <-- try to
					 * close the order System.out.print("Order Close failed, order number: ",
					 * OrderTicket(), " Error: ", GetLastError()); // <-- if the Order Close failed
					 * print some helpful information
					 */
				}

			}
			/*
			 * ositionShort = 0.0; positionCounterShort = 0.0; averagePriceShort = 0.0;
			 */
		}

		return Map.of(this.underlying, orders);
	}

	// +------------------------------------------------------------------+
	// | Close the order with the worst price, i.e. the long position |
	// | with the highest price (if input true) or the short position |
	// | with the lowest price (if input is false). |
	// +------------------------------------------------------------------+
	Map<String, ArrayList<Order>> CloseWorst(Boolean longPosition, OrderManagement orderManagement,
			QuoteDataFeed quoteDataFeed) {
		int id1 = -1, id2 = -1;
		double worstPrice;
		ArrayList<Order> orders = new ArrayList<Order>();
		Order worst_order_close = new Order();

		if (longPosition && positionLong > 1e-5) {
			worstPrice = averagePriceLong;
			for (Order order_active : orderManagement.GetOrdersActive("FX").get(this.underlying)) // <-- for loop to
																									// through all
																									// Orders
			{

				if (order_active.GetIdentifier2() == setting.Index() // --- magic number match
						&& order_active.GetType() == "BUY") // --- match sell orders with the short strategy
				{
					if (order_active.GetOpenReferencePrice() >= worstPrice) {

						worstPrice = order_active.GetOpenReferencePrice();
						worst_order_close = Order.Close(order_active, quoteDataFeed, "comment");
						id1 = order_active.GetIdentifier1();
						id2 = order_active.GetIdentifier2();
					}
				}
			} // --- end for loop

			if (id1 >= 0) {
				System.out.print("To close worst long position " + Integer.toString(setting.Index())+"\n");
				orders.add(worst_order_close);
				return Map.of(this.underlying, orders);
			}
		} // --- end long case
		else if (positionShort > 1e-5) {
			worstPrice = averagePriceShort;
			for (Order order_active : orderManagement.GetOrdersActive("FX").get(this.underlying)) // <-- for loop to
																									// loop through all
																									// Orders
			{

				if (order_active.GetIdentifier2() == -setting.Index() // --- magic number match
						&& order_active.GetType() == "SELL") // --- match sell orders with the short strategy
				{
					if (order_active.GetOpenReferencePrice() <= worstPrice) {
						worstPrice = order_active.GetOpenReferencePrice();
						worst_order_close = Order.Close(order_active, quoteDataFeed, "comment");
						id1 = order_active.GetIdentifier1();
						id2 = order_active.GetIdentifier2();
					}
				}
			} // --- end for loop

			if (id1 >= 0) {
				System.out.print("To close worst short position " + Integer.toString(setting.Index())+"\n");
				orders.add(worst_order_close);
				return Map.of(this.underlying, orders);
			}
		} // --- end short case

		return Map.of(this.underlying, orders);

	}

	// +------------------------------------------------------------------+
	// | Close the best long order (the one with the lowest price, if |
	// | the input is true, otherwise close the best short order, i.e. |
	// | the short order with the highest price. |
	// +------------------------------------------------------------------+
	Map<String, ArrayList<Order>> CloseBest(Boolean longPosition, OrderManagement orderManagement,
			QuoteDataFeed quoteDataFeed) {
		int id1 = -1, id2 = -1;
		double bestPrice;
		ArrayList<Order> orders = new ArrayList<Order>();
		Order best_order_close = new Order();

		if (longPosition && positionLong > 1e-5) {
			bestPrice = averagePriceLong;
			for (Order order_active : orderManagement.GetOrdersActive("FX").get(this.underlying)) // <-- for loop to
																									// through all
																									// Orders
			{

				if (order_active.GetIdentifier2() == setting.Index() // --- magic number match
						&& order_active.GetType() == "BUY") // --- match sell orders with the short strategy
				{
					if (order_active.GetOpenReferencePrice() <= bestPrice) {

						bestPrice = order_active.GetOpenReferencePrice();
						best_order_close = Order.Close(order_active, quoteDataFeed, "comment");
						id1 = order_active.GetIdentifier1();
						id2 = order_active.GetIdentifier2();
					}
				}
			} // --- end for loop

			if (id1 >= 0) {
				System.out.print("To close worst long position " + Integer.toString(setting.Index()));
				orders.add(best_order_close);
				return Map.of(this.underlying, orders);
			}
		} // --- end long case
		else if (positionShort > 1e-5) {
			bestPrice = averagePriceShort;
			for (Order order_active : orderManagement.GetOrdersActive("FX").get(this.underlying)) // <-- for loop to
																									// loop through all
																									// Orders
			{

				if (order_active.GetIdentifier2() == -setting.Index() // --- magic number match
						&& order_active.GetType() == "SELL") // --- match sell orders with the short strategy
				{
					if (order_active.GetOpenReferencePrice() >= bestPrice) {
						best_order_close = Order.Close(order_active, quoteDataFeed, "comment");
						bestPrice = order_active.GetOpenReferencePrice();
						id1 = order_active.GetIdentifier1();
						id2 = order_active.GetIdentifier2();
					}
				}
			} // --- end for loop

			if (id1 >= 0) {
				System.out.print("To close worst short position " + Integer.toString(setting.Index())+"\n");
				orders.add(best_order_close);
				return Map.of(this.underlying, orders);
			}
		} // --- end short case

		return Map.of(this.underlying, orders);

	}

	// +------------------------------------------------------------------+
	// | Function that checks for the minimum lot size. The user can |
	// | Adjust it as desired. Recommended: 0.05. |
	// +------------------------------------------------------------------+
	double CheckOrderSize(double size) {
		return size < 0.05 ? 0.05 : NormalizeDouble(size, 2);
	}

	// +------------------------------------------------------------------+
	// | Function that initialises the measure based on a String input. |
	// +------------------------------------------------------------------+
	void InitializeMeasure(String measure) {
		if (measure == "lin")
			measureFunc = new MeasureLinear();
		else if (measure == "exp")
			measureFunc = new MeasureExponential();
		else if (measure == "perf")
			measureFunc = new MeasurePerformance();
		else
			System.out.print("measurement " + measure + " not implemented\n");
	}
	// +------------------------------------------------------------------+

	// +------------------------------------------------------------------+
	// | Close the x best and y worst orders of the long side |
	// | (if parameter longPosition is true) or the short side if the |
	// | parameter is set to false. |
	// +------------------------------------------------------------------+
	Map<String, ArrayList<Order>> CloseBestNWorst(Boolean longPosition, int best, int worst,
			OrderManagement orderManagement, QuoteDataFeed quoteDataFeed) {

		ArrayList<Order> relevant_orders = orderManagement.GetOrdersActive("FX").get(this.underlying);

		if (longPosition)
			relevant_orders.removeIf(x -> x.GetType() == "SELL");
		else
			relevant_orders.removeIf(x -> x.GetType() == "BUY");

		int noBest = best, noWorst = worst;

		if (longPosition && positionLong > 1e-5) {
			// --- collect order tickets
			// GetTicketsLong(ticketsAll, prices);
			if (relevant_orders.size() == 0)
				return Map.of(this.underlying, new ArrayList<Order>());

			// --- check if just all orders can be closed
			if (relevant_orders.size() <= Math.min(worst, best)) {
				System.out.print("Closing all long positions of strategy " + Integer.toString(setting.Index())+"\n");
				return this.CloseAllOrders(longPosition, quoteDataFeed, orderManagement);
			} else {
				ArrangeClosing(relevant_orders.size(), noWorst, noBest);
				System.out.print("Closed long positions best:worst " + Integer.toString(best) + ":"
						+ Integer.toString(worst) + " of strategy " + Integer.toString(setting.Index())+"\n");

				ArrayList<Order> ordersToClose = this.CloseOrdersBest(orderManagement, quoteDataFeed, noBest,
						longPosition);// --- best orders
				ordersToClose.addAll(this.CloseOrdersWorst(orderManagement, quoteDataFeed, noWorst, longPosition));
				return Map.of(this.underlying, ordersToClose);

			} // --- end long ticket check
		} // --- end long case
		else if (positionShort > 1e-5) {
			if (relevant_orders.size() == 0)
				return Map.of(this.underlying, new ArrayList<Order>());

			// --- check if just all orders can be closed
			if (relevant_orders.size() <= Math.min(worst, best)) {
				System.out.print("Closing all short positions of strategy " + Integer.toString(setting.Index())+"\n");
				return this.CloseAllOrders(longPosition, quoteDataFeed, orderManagement);
			} else {
				ArrangeClosing(relevant_orders.size(), noWorst, noBest);
				System.out.print("Closed short positions best:worst " + Integer.toString(best) + ":"
						+ Integer.toString(worst) + " of strategy " + Integer.toString(setting.Index())+"\n");

				ArrayList<Order> ordersToClose = this.CloseOrdersBest(orderManagement, quoteDataFeed, noBest,
						longPosition);// --- best orders
				ordersToClose.addAll(this.CloseOrdersWorst(orderManagement, quoteDataFeed, noWorst, longPosition));
				return Map.of(this.underlying, ordersToClose);
			} // --- end short case

		}
		
		return null;
	}
	// +------------------------------------------------------------------+

	// +------------------------------------------------------------------+
	// | Close the last x (parameter bottom) order tickets of the input.|
	// +------------------------------------------------------------------+
	public ArrayList<Order> CloseOrdersWorst(OrderManagement orderManagement, QuoteDataFeed quoteDataFeed, int bottom,
			boolean longPosition) {
		quoteDataFeed.Refresh();
		ArrayList<Order> orders_to_close = new ArrayList<Order>();
		ArrayList<Order> relevant_orders = orderManagement.GetOrdersActive("FX").get(this.underlying);

		if (longPosition) {
			relevant_orders.removeIf(x -> x.GetType() == "SELL");
			Collections.sort(relevant_orders, (o1, o2) -> Order.compareOpenPrice(o1, o2));
		} else {
			relevant_orders.removeIf(x -> x.GetType() == "BUY");
			Collections.sort(relevant_orders, (o1, o2) -> Order.compareOpenPrice(o2, o1));
		}

		int orders_closed = 0;
		for (Order order_active : relevant_orders) {
			if (orders_closed >= bottom)
				break;

			orders_to_close.add(Order.Close(order_active, quoteDataFeed, "comment"));
			orders_closed += 1;
		} // --- close for loop for worst orders (long positions with highest prices)

		return orders_to_close;
	}

	// +------------------------------------------------------------------+
	// | Close the x (parameter top) top orders of the ticket list. |
	// +------------------------------------------------------------------+
	public ArrayList<Order> CloseOrdersBest(OrderManagement orderManagement, QuoteDataFeed quoteDataFeed, int bottom,
			boolean longPosition) {
		quoteDataFeed.Refresh();
		ArrayList<Order> orders_to_close = new ArrayList<Order>();
		ArrayList<Order> relevant_orders = orderManagement.GetOrdersActive("FX").get(this.underlying);

		if (longPosition) {
			relevant_orders.removeIf(x -> x.GetType() == "SELL");
			Collections.sort(relevant_orders, (o1, o2) -> Order.compareOpenPrice(o2, o1));
		} else {
			relevant_orders.removeIf(x -> x.GetType() == "BUY");
			Collections.sort(relevant_orders, (o1, o2) -> Order.compareOpenPrice(o1, o2));
		}

		int orders_closed = 0;
		for (Order order_active : relevant_orders) {
			if (orders_closed >= bottom)
				break;

			orders_to_close.add(Order.Close(order_active, quoteDataFeed, "comment"));
			orders_closed += 1;
		} // --- close for loop for worst orders (long positions with highest prices)

		return orders_to_close;
	}

	// +------------------------------------------------------------------+

	// +------------------------------------------------------------------+
	// | Function to adjust a integer pair (worst, best) for closure |
	// | of the worst and best orders if the overall order count is |
	// | too small, we deduct one of each until best+worst is less |
	// | than the order count. |
	// +------------------------------------------------------------------+
	void ArrangeClosing(int arraySize, int worst, int best) {
		for (int i = 0; i < arraySize; i++) {
			if (arraySize < worst + best && Math.min(worst, best) >= 0) {
				worst -= 1;
				best -= 1;
			} else
				break;
		}

	}
	// +------------------------------------------------------------------+

	// +------------------------------------------------------------------+
	// | Function to define barrier levels for skewing, |
	// | in use currently, looking forward, |
	// | that should be input parameter. |
	// +------------------------------------------------------------------+
	ArrayList<Double>  DefineBarrierLevels(double orderSize) {
		if (orderSize <= 0.2) {
			return  new ArrayList<>(Arrays.asList(5.0, 10.0));
		} else if (orderSize > 0.2 && orderSize <= 0.5) {
			return new ArrayList<>(Arrays.asList(5.0, 10.0, 15.0));

		} else {
			return new ArrayList<>(Arrays.asList(2.5, 5.0, 7.5, 10.0));
		}

	}
	// +------------------------------------------------------------------+

	// +------------------------------------------------------------------+
	// | A function that outsources some code from the constructor. |
	// +------------------------------------------------------------------+
	void InitializeFields() {

		// -- end of updating average price and position sizes

		this.factorProfitLong = this.factorProfitShort = 0.667;
		this.positionLong = this.positionShort = 0.0;
		this.averagePriceLong = this.averagePriceShort = 0.0;
		this.shrinkFlong = this.shrinkFshort = 1.0;
		this.fractionLong = this.fractionShort = 1.0;
		this.positionCounterLong = this.positionCounterShort = 0.0;
		this.eventLong = this.eventShort = 0;

		this.profitBasketLong = this.profitBasketShort = 0.0;
	}

	public double NormalizeDouble(double number, int digits) {
		return number;
	}
	// +------------------------------------------------------------------+


public void FetchQuoteData(QuoteDataFeed quoteDataFeed)
{
	for(EventEstimator estimator: this.eEstimatorDown)
		{estimator.FetchQuoteData(quoteDataFeed);}

	for(EventEstimator estimator: this.eEstimatorUp)
		{estimator.FetchQuoteData(quoteDataFeed);}

		this.eEstimator.FetchQuoteData(quoteDataFeed);
}
}



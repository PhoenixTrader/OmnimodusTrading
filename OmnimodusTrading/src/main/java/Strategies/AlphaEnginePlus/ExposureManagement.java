package AlphaEnginePlus;

import java.util.ArrayList;
import java.util.Arrays;

//+------------------------------------------------------------------+
//|   The class that includes all values related to exposure,        |
//|   position sizes, barriers, order counts and more.               |
//+------------------------------------------------------------------+
public class ExposureManagement {

	private ArrayList<Double> exposureLevels;
	private ArrayList<Double> exposureBarriers;
	private ArrayList<Double> exposureLong;
	private ArrayList<Double> exposureShort;
	private ArrayList<Double> marginBarriers;
	private double netExposure;
	private int maxOrderCount;

	public ExposureManagement() {
	};
	// --- Update field values for a specific index

	// --- get functions
	public double GetNetExposure() {
		return netExposure;
	}

	public double GetTotalLongPositions() {
		return Sum(exposureLong);
	}

	public double GetTotalShortPositions() {
		return Sum(exposureShort);
	}

	public double GetExposureBarrierLevel(int index) {
		return exposureBarriers.get(index >= exposureBarriers.size() ? exposureBarriers.size() - 1 : index);
	}

	public double GetCleanExposure(int index) {
		return SumExcept(exposureLevels, index);
	}

	public double GetLiquidityBarrier() {
		return 1.0;
	}

	// -- function to check whether the order count is about to overflow
	// boolean CheckOrderCount() {return maxOrderCount - 50 >= OrdersTotal();}
	public boolean CheckBarrierLevelLong(int index) {
		return CheckBarrierSingle(netExposure, index);
	}

	public boolean CheckBarrierLevelShort(int index) {
		return CheckBarrierSingle(-netExposure, index);
	}

//+------------------------------------------------------------------+

//+------------------------------------------------------------------+
//|   Simple constructor via copy-paste.                             |
//+------------------------------------------------------------------+
	ExposureManagement(int numberPairs, ArrayList<Double> eBarriers, ArrayList<Double> mBarriers) {
		
		this.exposureBarriers = eBarriers;
		this.marginBarriers = mBarriers;
		this.exposureLevels = new ArrayList<Double>();
		this.exposureLong = new ArrayList<Double>();
		this.exposureShort = new ArrayList<Double>();
		for (int i = 0; i < 10; i++) {
			this.exposureLevels.add(0.0);
			this.exposureShort.add(0.0);
			this.exposureLong.add(0.0);
			}		
		this.netExposure = 0.0;
		this.maxOrderCount = 500;
	}
//+------------------------------------------------------------------+

//+------------------------------------------------------------------+
//|   Update position management after a trade happened.             |
//+------------------------------------------------------------------+
	void UpdatePosition(int index, double longPos, double shortPos) {
		this.exposureLevels.set(index, longPos - shortPos);
		this.exposureLong.set(index, longPos);
		this.exposureShort.set(index, shortPos);
		this.netExposure = SumExcept(exposureLevels, index) + exposureLevels.get(index);
	}
//+------------------------------------------------------------------+

//+------------------------------------------------------------------+
//|   Update position management after only a long trade.            |
//+------------------------------------------------------------------+
	void UpdatePositionLong(int index, double longPos) {
		this.exposureLong.set(index, longPos);
		this.exposureLevels.set(index, longPos - exposureShort.get(index));
		this.netExposure = SumExcept(exposureLevels, index) + exposureLevels.get(index);
	}

//+------------------------------------------------------------------+
//|   Update position management after only a short trade.           |
//+------------------------------------------------------------------+
	void UpdatePositionShort(int index, double shortPos) {
		exposureShort.set(index, shortPos);
		exposureLevels.set(index, exposureLong.get(index) - shortPos);
		netExposure = SumExcept(exposureLevels, index) + exposureLevels.get(index);
	}

//+------------------------------------------------------------------+
//|   Check if the net exposure is betwen the indexed barriers,      |
//|   long version.                                                  |
//+------------------------------------------------------------------+
	boolean CheckBarrierBetweenLong(int indexLow, int indexHigh) {
		return CheckBarriers(netExposure, indexLow, indexHigh);
	}
//+------------------------------------------------------------------+

//+------------------------------------------------------------------+
//|   Check if the net exposure is betwen the indexed barriers,      |
//|   short version.                                                 |
//+------------------------------------------------------------------+
	boolean CheckBarrierBetweenShort(int indexLow, int indexHigh) {
		return CheckBarriers(-netExposure, indexLow, indexHigh);
	}
//+------------------------------------------------------------------+

//+------------------------------------------------------------------+
//|   Check if the input value lies between the values of the        |
//|   barrier array with the given indexes.                          |
//+------------------------------------------------------------------+
	boolean CheckBarriers(double value, int indexLow, int indexHigh) {
		if (indexHigh >= exposureBarriers.size() && indexLow >= exposureBarriers.size())
			return value >= exposureBarriers.get(exposureBarriers.size() - 1);
		else if (indexHigh >= exposureBarriers.size() && indexLow < exposureBarriers.size())
			return value >= exposureBarriers.get(indexHigh);
		else
			return value >= exposureBarriers.get(indexHigh) && value < exposureBarriers.get(indexHigh);
	}
//+------------------------------------------------------------------+

//+------------------------------------------------------------------+
//|   Check if the input value lies above the value with the given   |
//|   index of the barrier array
//+------------------------------------------------------------------+
	boolean CheckBarrierSingle(double value, int index) {
		if (index >= exposureBarriers.size())
			return value >= exposureBarriers.get(exposureBarriers.size() - 1);
		else
			return value >= exposureBarriers.get(index);
	}
//+------------------------------------------------------------------+

//====================================================================
//=== not in use, but eventually useful later on =====================
//====================================================================
	/*
	 * //+------------------------------------------------------------------+ //|
	 * Class that allows the flexible implementation of various | //| functions to
	 * measure a change (like performance, absolute etc.)|
	 * //+------------------------------------------------------------------+ class
	 * iBarrierCheck { virtual boolean CheckBarrier(double value, int indexLow, int
	 * indexHigh, double& barriers[]) = 0; virtual boolean CheckBarrierSingle(double
	 * value, int index, double& barriers[]) = 0; };
	 * 
	 * 
	 * //+------------------------------------------------------------------+ //|
	 * Absolute difference. |
	 * //+------------------------------------------------------------------+ class
	 * BarrierTwo : public iBarrierCheck {
	 * 
	 * boolean CheckBarrier(double value, int indexLow, int indexHigh,
	 * ArrayList<Double> barriers); boolean CheckBarrierSingle(double value, int
	 * index, ArrayList<Double> barriers); };
	 * 
	 * //+------------------------------------------------------------------+ //| |
	 * //+------------------------------------------------------------------+
	 * boolean BarrierTwo::CheckBarrier(double value, int indexLow, int indexHigh,
	 * double &barriers[]) { return true;
	 * 
	 * }
	 * 
	 * 
	 * //+------------------------------------------------------------------+ //| |
	 * //+------------------------------------------------------------------+
	 * boolean BarrierTwo::CheckBarrierSingle(double value, int index,double
	 * &barriers[]) { return true;
	 * 
	 * }
	 */
//+------------------------------------------------------------------+

	public static double Sum(ArrayList<Double> dList) {
		double sum = 0;
		for (int i = 0; i < dList.size(); i++)
			sum += dList.get(i);
		return sum;
	}

	public static double SumExcept(ArrayList<Double> dList, int index) {
		double sum = 0;
		for (int i = 0; i < dList.size(); i++)
			sum += dList.get(i);
		return sum - dList.get(index);
	}
}
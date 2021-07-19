package AlphaEnginePlus;

import java.util.ArrayList;

//+------------------------------------------------------------------+
//|   The class that includes fixed trading parameters for a         |
//|   single coast trading pair. The implementation is used to       |
//|   increase the readability of the overall code.                  |
//+------------------------------------------------------------------+
class CoastTradingSetting {

	private String fxRate; // --- current symbol that is traded
	private int indexNumber; // --- index of the coast trading pair

	class Long {
		private double deltaUp, deltaDown, deltaOriginal;
		private double orderSize;
		private ArrayList<Double> barrierLevels;

		// +------------------------------------------------------------------+
		// | Constructor of long settings. |
		// +------------------------------------------------------------------+
		public Long(double dUp, double dDown, double dOriginal, double lotsBase, ArrayList<Double> barriers) {
			this.deltaUp = dUp;
			this.deltaDown = dDown;
			this.deltaOriginal = dOriginal;
			this.orderSize = lotsBase;
			this.barrierLevels = barriers;
		}

		public double GetDeltaUp() {
			return deltaUp;
		}

		public double GetDeltaDown() {
			return deltaDown;
		}

		public double GetSize() {
			return orderSize;
		}

		public double GetBarrier(int index) {
			return barrierLevels.get(index >= barrierLevels.size() ? barrierLevels.size() - 1 : index);
		}

		// --- Set Functions
		void SetDeltas(double dUp, double dDown) {
			this.deltaUp = dUp;
			this.deltaDown = dDown;
		}
	};

	class Short {

		private double deltaUp, deltaDown, deltaOriginal;
		private double orderSize; // --- current symbol that is traded
		private ArrayList<Double> barrierLevels;

		// --- constructors
		public Short() {
		};

		// +------------------------------------------------------------------+
		// | Constructor of short settings. |
		// +------------------------------------------------------------------+
		public Short(double dUp, double dDown, double dOriginal, double lotsBase, ArrayList<Double> barriers) {
			this.deltaUp = dUp;
			this.deltaDown = dDown;
			this.deltaOriginal = dOriginal;
			this.orderSize = lotsBase;
			this.barrierLevels = barriers;
		}

		// --- Get functions
		double GetDeltaUp() {
			return deltaUp;
		}

		double GetDeltaDown() {
			return deltaDown;
		}

		double GetSize() {
			return orderSize;
		}

		double GetBarrier(int index) {
			return barrierLevels.get(index >= barrierLevels.size() ? barrierLevels.size() - 1 : index);
		}

		// --- Set Functions
		void SetDeltas(double dUp, double dDown) {
			deltaUp = dUp;
			deltaDown = dDown;
		}
	};

	private Long settingLong;
	private Short settingShort;

	// --- constructors
	public CoastTradingSetting() {
	};

	// --- The following functions are just for the sake of readability
	// --- These only return field values and prohibit any external change
	public double GetSizeShort() {
		return settingShort.GetSize();
	}

	public double GetSizeLong() {
		return settingLong.GetSize();
	}

	public double GetDeltaDownLong() {
		return settingLong.GetDeltaDown();
	}

	public double GetDeltaDownShort() {
		return settingShort.GetDeltaDown();
	}

	public double GetDeltaUpLong() {
		return settingLong.GetDeltaUp();
	}

	public double GetDeltaUpShort() {
		return settingShort.GetDeltaUp();
	}

	public int Index() {
		return indexNumber;
	}

//+------------------------------------------------------------------+

//+------------------------------------------------------------------+
//|   Constructor multiple deltas.                                   |
//+------------------------------------------------------------------+
	public CoastTradingSetting(double dUp, double dDown, double dOriginal, double lotsBase, ArrayList<Double> barriers,
			int index) {
		indexNumber = index;
		this.settingLong = new Long(dUp, dDown, dOriginal, lotsBase, barriers);
		this.settingShort = new Short(dUp, dDown, dOriginal, lotsBase, barriers);
	}
//+------------------------------------------------------------------+

//+------------------------------------------------------------------+
//|   Constructor detailed.                                          |
//+------------------------------------------------------------------+
	CoastTradingSetting(double dUpL, double dDownL, double dOriginalL, double lotsBaseL, ArrayList<Double> barriersLong,
			double dUpS, double dDownS, double dOriginalS, double lotsBaseS, ArrayList<Double> barriersShort,
			int index) {
		indexNumber = index;
		settingLong = new Long(dUpL, dDownL, dOriginalL, lotsBaseL, barriersLong);
		settingShort = new Short(dUpS, dDownS, dOriginalS, lotsBaseS, barriersShort);
	}
//+------------------------------------------------------------------+

//+------------------------------------------------------------------+
//|   Function to set the deltas.                                    |
//+------------------------------------------------------------------+
	void SetDeltas(double dUp, double dDown, int ls) {
		if (ls == 1)
			settingLong.SetDeltas(dUp, dDown);
		else if (ls == -1)
			settingShort.SetDeltas(dUp, dDown);

	}

//+------------------------------------------------------------------+
//|   Returns the long barrier of the trading pair at the index.     |
//+------------------------------------------------------------------+
	double GetBarrierLong(int index) {
		return settingLong.GetSize() * settingLong.GetBarrier(index);
	}
//+------------------------------------------------------------------+

//+------------------------------------------------------------------+
//|   Returns the short barrier of the trading pair at the index.    |
//+------------------------------------------------------------------+
	double GetBarrierShort(int index) {
		return settingShort.GetSize() * settingShort.GetBarrier(index);
	}
//+------------------------------------------------------------------+

};

package Strategies.Measures;

//+------------------------------------------------------------------+
//|   Exponential change.                                            |
//+------------------------------------------------------------------+
public class MeasureExponential implements iMeasure
{
public double Change(double newVal, double oldVal) 
{ 
	return Math.log(newVal / oldVal); 
	}
};

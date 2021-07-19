package Measures;


//+------------------------------------------------------------------+
//|   Performance.                                                   |
//+------------------------------------------------------------------+
public class MeasurePerformance implements iMeasure
{
public double            Change(double newVal, double oldVal) { return newVal / oldVal - 1.0; }
};
//+------------------------------------------------------------------+


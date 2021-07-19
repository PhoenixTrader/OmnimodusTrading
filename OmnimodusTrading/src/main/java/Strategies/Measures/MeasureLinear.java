package Measures;

//+------------------------------------------------------------------+
//|   Absolute difference.                                           |
//+------------------------------------------------------------------+
public class MeasureLinear implements  iMeasure
{
public double   Change(double newVal, double oldVal) { return newVal - oldVal; }
};

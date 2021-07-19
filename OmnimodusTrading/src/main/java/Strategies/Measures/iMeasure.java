package Measures;

//+------------------------------------------------------------------+
//|   Interface that allows the flexible implementation of various   |
//|   functions to measure a change (like performance, absolute etc.)|
//+------------------------------------------------------------------+
public interface iMeasure
{
  public double    Change(double newVal, double oldVal);
}




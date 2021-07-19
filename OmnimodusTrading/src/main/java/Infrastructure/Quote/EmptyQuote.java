package Quote;

import java.time.ZonedDateTime;

public class EmptyQuote extends Quote
{
	
	public EmptyQuote() {};
	
	
	public String GetSymbol()
	{
		return "No Symbol";
	}
	
	public double GetBid()
	{
		return Double.NaN;
	}
	
	public double GetAsk()
	{
		return Double.NaN;
	}
	
	public boolean IsUpdated(ZonedDateTime comparableTime)
	{
		return false; 
	}
	
	public ZonedDateTime GetTime()
	{
		return null;
	}
	
	public String Print()
	{
		return  "{Empty Quote}";
	}

}

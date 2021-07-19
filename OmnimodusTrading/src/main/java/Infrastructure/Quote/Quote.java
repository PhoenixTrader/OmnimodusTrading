package Infrastructure.Quote;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Quote  implements Cloneable
{
	private ZonedDateTime time;
	private double bid;
	private double ask;
	private double askVolume;
	private double bidVolume;
	private String symbol;
	
	public Quote(String symbol, ZonedDateTime time, double bid, double ask)
	{
		this.bid = bid;
		this.ask = ask;
		this.time = time;
		this.symbol = symbol;
	}
	
	public Quote(String symbol, ZonedDateTime time, double bid, double ask, double bidVol, double askVol)
	{
		this.time = time;
		this.bid = bid;
		this.ask = ask;
		this.symbol = symbol;
		this.bidVolume = bidVol;
		this.askVolume = askVol;
	}
	
	
	public Quote() {
		this.bid = Double.NaN;
		this.ask = Double.NaN;
		this.time = null;
		this.askVolume = 0.0;
		this.bidVolume = 0.0;
	};
	
	public String GetSymbol()
	{
		return symbol;
	}
	
	public ZonedDateTime GetTime()
	{
		return time;
	}
	
	public double GetBid()
	{
		return this.bid;
	}
	
	public double GetAsk()
	{
		return this.ask;
	}
	
	public double GetMid()
	{ return (this.bid + this.ask) / 2.0;
	}

	
	public boolean IsUpdated(ZonedDateTime comparableTime)
	{
		return this.GetTime().isAfter(comparableTime); 
	}
	
	public String Print()
	{
		try {
		return  "{symbol: " + this.GetSymbol() + ": {time: " + this.GetTime().format(DateTimeFormatter.ISO_DATE_TIME) + ", bid: " + Double.toString(this.GetBid()) + ", ask: " + Double.toString(this.GetAsk()) + "}}\n";
		} catch(Exception e)
		{
			return "{error printing quote}\n";
		}
		
		}
	
	public void SetSymbol(String newSymbol)
	{
		this.symbol = newSymbol;
	}


	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}


}

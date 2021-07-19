package DataLoader.CandleStick;


import Infrastructure.Quote.*;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class CandleStick implements Serializable {

	double open;
	double close;
	double high;
	double low;
	double volume;
	ZonedDateTime time;
	String symbol;

	public CandleStick() {
	};

	public CandleStick(String symbol, ZonedDateTime time, double open, double high, double low, double close, double volume) {
		this.symbol = symbol;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.time = time;
	};
	
	public static CandleStick CandleStickFromString(String symbol, String[] inputCSV) { 
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
		//-- 0 and 1: date and time
		//System.out.print("TEST\n");
		//System.out.print("FIRST: " + inputCSV[0]);
		//System.out.print("\nSECOND: " + inputCSV[1]);
		//System.out.print(inputCSV[0] + " " + inputCSV[1]);
		
		ZonedDateTime time = ZonedDateTime.parse(inputCSV[0] + " " + inputCSV[1], formatter.withZone(ZoneId.systemDefault()));
		//-- 1 time
		double open = Double.parseDouble(inputCSV[2]);
		
		double high = Double.parseDouble(inputCSV[3]);
				
		double low = Double.parseDouble(inputCSV[4]);
		
		double close = Double.parseDouble(inputCSV[5]);
		
		double volume = Double.parseDouble(inputCSV[5]);
		

		return new CandleStick(symbol, time, open, high, low, close, volume);
		
	};

	public Quote[] Expand(int i, double spread) {
		
		Quote first = new Quote(this.symbol, this.time, this.open - spread, this.open + spread, this.volume / (double)i, this.volume / (double)i);
		Quote second = new Quote(this.symbol, this.time.plusSeconds(30), this.high - spread, this.high + spread, this.volume / (double)i, this.volume / (double)i);
		Quote third = new Quote(this.symbol, this.time.plusSeconds(45), this.low - spread, this.low + spread, this.volume / (double)i, this.volume / (double)i);
		Quote fourth = new Quote(this.symbol, this.time.plusSeconds(55), this.close - spread, this.close + spread, this.volume / (double)i, this.volume / (double)i);
		
		Quote[] quoteList = new Quote[i];
		
		quoteList[0] = first;
		quoteList[1] = second;
		quoteList[2] = third;
		quoteList[3] = fourth;
		
		return quoteList;
		
	}

	public Quote GetCloseQuote(double spread)
	{
		return new Quote(this.symbol, this.time.plusSeconds(55), this.close - spread, this.close + spread, this.volume, this.volume);
	}

}

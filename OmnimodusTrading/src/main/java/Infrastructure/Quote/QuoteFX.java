package Infrastructure.Quote;
import Infrastructure.Quote.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class QuoteFX extends Quote
{
	/*
	private ZonedDateTime time;
	private double bid;
	private double ask;
	private String symbol;
	private double bidVolume;
	private double askVolume;*/
	
	
	public QuoteFX(String symbol, ZonedDateTime time, double bid, double ask) 
	{
		super(symbol, time, bid, ask);
		}

	
	public QuoteFX(String symbol, ZonedDateTime time, double bid, double ask, double bidVol, double askVol) {
		super(symbol, time, bid, ask, bidVol, askVol);
	}
	
	public QuoteFX() {super();};
	

	
	

}

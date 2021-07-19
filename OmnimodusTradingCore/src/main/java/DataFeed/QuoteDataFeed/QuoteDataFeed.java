package DataFeed.QuoteDataFeed;
import DataFeed.Connector.*;
import Infrastructure.Quote.*;

import java.util.HashMap;
import java.util.Map;
import java.time.ZonedDateTime;


public class QuoteDataFeed {
	
	 Map<String, Map<String, Quote>> liveQuotes; // it is mapped as follows asset type -> symbol -> quote
	 ConnectorGeneric connector;
	 
	 public QuoteDataFeed(String[] assetClasses, ConnectorGeneric connector)
	 {
		 this.liveQuotes = new HashMap<String, Map<String, Quote>>();
		 
		 for(int i =0; i<assetClasses.length; i++)
			 this.liveQuotes.put(assetClasses[i], new HashMap<String, Quote>());
		 
		 this.connector = connector;
	 };
	 
	 
	 public QuoteDataFeed(String[] assetClasses, String[] symbols, ConnectorGeneric connector)
	 {
		 this.liveQuotes = new HashMap<String, Map<String, Quote>>();
		 
		 for(int i =0; i<assetClasses.length; i++)
		 {
			 this.liveQuotes.put(assetClasses[i], new HashMap<String, Quote>());
			 for( int j = 0; j < symbols.length; j++)
			 {
				 this.liveQuotes.get(assetClasses[i]).put(symbols[j], new Quote()); //-- dummy null values just that the dictionary exists
			 }
		 }
		 
		 this.connector = connector;
	 };
	 
	 public void AddFirst(String assetClass, String symbol, ZonedDateTime time, double bid, double ask)
	 {
		 this.liveQuotes.get(assetClass).put(symbol, new QuoteFX(symbol, time, bid, ask));
	 }
	 
	 public void RefreshStatic(String assetClass, String symbol, Quote price)
	 {
		 this.liveQuotes.get(assetClass).put(symbol, price);
	 }
	 
	 
	 public void RefreshStatic(String assetClass, String symbol, ZonedDateTime time, double bid, double ask)
	 {

		 if(this.liveQuotes.get(assetClass).get(symbol).IsUpdated(time))
			 this.liveQuotes.get(assetClass).put(symbol, new QuoteFX(symbol, time, bid, ask));
			 
	 }
	 
	 public void RefreshStatic(String assetClass, Quote quote)
	 {
		 //System.out.print("now");
		 quote.Print();
		 this.liveQuotes.get(assetClass).put(quote.GetSymbol(), quote);
			 
	 }
	 
	 public String PrintForAssetClass(String assetClass)
	 {
		 String res ="";
		 for (Map.Entry<String, Quote> entry : this.liveQuotes.get(assetClass).entrySet())
			 res += entry.getValue().Print() + "\n";
			 
		return res.substring(0, res.length() - 2);
	 }
	 
	 public Quote GetQuote(String assetClass, String symbol)
	 {
		 return this.liveQuotes.get(assetClass).get(symbol);
	 }
	 
	 public void Refresh()
	 {}
	 
	 public Quote GetFXQuote(String symbol)
	 {
		 return this.liveQuotes.get("FX").get(symbol);
	 }

public void PrintFXLiveQuotes()
{
	for(String underlying : this.liveQuotes.get("FX").keySet())
		this.liveQuotes.get("FX").get(underlying).Print();
}

	/*
	 def __init__(self, prices = {}, connector = None):
	        this.liveQuotes = dict(prices)
	        this.historicalPrices = {} # to come
	        this.connector = connector
	        
	    def AddConnector(self, connector, api = ''):
	        this.connector = connector
	    
	    # TODO for life feeds, the refresh function should update bid, ask and time
	    # the exact structure in the end is determined by the APIs
	    # The following is just a brief idea
	    def Refresh(self, source):
	        if source == "MT4":
	            this.connector.fetch()
	            this.bid = this.connector.newBid
	            this.ask = this.connector.newAsk
	            this.time  = this.connector.lastUpdateTime
	            
	        elif source == "IB":
	            this.a = 0
	    # sets up the connector for the live data stream
	    def SetUpConnectorZeroMQ(self, connector, symbolList):
	        this.connector = connector
	        this.connector.SendTrackPricesRequest(symbolList)
	        for symbol in symbolList: # subscribes to live data stream for the symbols
	            this.connector.SubscribeMarketData(symbol)
	            
	    # updates the prices in the context of the zeromq connector
	    def UpdatePricesZeroMQ(self):
	        this.liveQuotes.update(this.connector.price_dictionary)
	        #except:
	        #    this.liveQuotes = this.connector._price_dictionary
	        
	    def UpdatePricesZeroMQExternal(self, connector):
	        this.liveQuotes.update(connector.price_dictionary)
	    
	    # returns the current live data of the given symbols as dictionary
	    def GetPriceZeroMQ(self):
	        return this.connector._price_dictionary
	    
	     # returns the last tick data of the given symbols as dictionary
	    def GetLastTickZeroMQ(self):
	        return this.connector._last_tick
	    
	    # that should mainly be used for backtesting
	    def AddData(self, symbol, time, bid, ask):
	        this.liveQuotes[symbol]= {"time": time, "bid" : bid, "ask": ask}

*/
}

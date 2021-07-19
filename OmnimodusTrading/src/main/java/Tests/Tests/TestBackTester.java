package Tests;

import java.io.FileReader;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Hashtable;
import java.util.List;
import org.jsoup.nodes.Document;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVParser;
import com.opencsv.CSVReader;

import CandleStick.CandleStick;
import Connector.ConnectorGeneric;
import QuoteDataFeed.QuoteDataFeed;
import AccountManagement.*;
import java.util.ArrayList;
import OrderManagement.*;
import java.util.*;
import Quote.Quote;
import Order.Order.*;

import AlphaEnginePlus.*;

public class TestBackTester {

	public static void BackTest() {
		// -------------- parameter definition ----------
		String underlying = "EURCHF";
		String ticks = "1";
		String frame = "minutely";
		double deltaArray[] = { 0.00125, 0.00175, 0.005 };
		double deltaOvershootScale[] = { 1.0, 1.0, 1.0 };

		double positionSizeArray[] = { 0.05, 0.5, 0.1 };
		double exposureBarrierLevels[] = { 0.5, 0.75, 1.0 , 2.0, 4.0};
		String measureThresholds = "lin";
		String fileID = "Test000001";
		String[] assetClasses = { "FX" };
		String[] symbols = {underlying};
		QuoteDataFeed quoteDataFeed = new QuoteDataFeed(assetClasses, symbols, new ConnectorGeneric());

		ArrayList<String> underlyingList = new ArrayList<String>();
		underlyingList.add(underlying);
		OrderManagement orderManagement = new OrderManagement(underlyingList);

		AccountManagement accountManagement = new AccountManagement(10000);
		AlphaEnginePlus aePlus = new AlphaEnginePlus(underlying, deltaArray, deltaOvershootScale, positionSizeArray,
				exposureBarrierLevels, measureThresholds, fileID, quoteDataFeed);

		int tickLimit = 400000;
		// -----------------------------

		double spread = 0.00000001;
		//CSVParser parser = new CSVParserBuilder().withSeparator(',').withIgnoreQuotations(true).build();

		var file = "E:/Projects/Algo Trading/Git/MonkeyTrading/HistoricalData/FX/"+ frame+"/"+underlying + ticks + ".csv"; // D:/MonkeyTrading/HistoricalData/Equity/minutely/test.csv
		int currentTick = 0;
		
		
		try {

			FileReader filereader = new FileReader(file);

			CSVReader csvReader = new CSVReader(filereader);
			String[] nextRecord;
			Quote[] quoteList;
			
			nextRecord = csvReader.readNext();
			Quote firstQuote = CandleStick.CandleStickFromString(underlying, nextRecord).GetCloseQuote(spread);
			quoteDataFeed.RefreshStatic("FX", firstQuote);
			aePlus.FetchQuoteData(quoteDataFeed);



			Map<String, ArrayList<Order.Order>> ordersFromStrategy;
			while ((nextRecord = csvReader.readNext()) != null) {
				if (currentTick >= tickLimit) {
					break;
				}
				for (Quote quote : CandleStick.CandleStickFromString(underlying, nextRecord).Expand(4, spread)) {

					quote.SetSymbol(underlying);
					
					//System.out.print(quote.Print() + "\n");
					
					quoteDataFeed.RefreshStatic("FX", quote);
					
					//quoteDataFeed.PrintFXLiveQuotes();

					ordersFromStrategy = aePlus.TradeAsymmetric(orderManagement, quoteDataFeed);
					
					if(!ordersFromStrategy.get(underlying).isEmpty())
						System.out.print("stop");
					orderManagement.FillAndFetch("FX", ordersFromStrategy);

					accountManagement.FetchData(orderManagement, quoteDataFeed);
				}

				if( currentTick % 1000000000 ==0 )
				{
				System.out.print("----------- status -------------\n");

				orderManagement.Print();

				accountManagement.Print();

				System.out.print("----------- status end -------------\n");
				}

				currentTick += 1;

				System.out.println();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.print("Error in Loop!\n");
		}
		
		System.out.print("----------- Final status -------------\n");

		orderManagement.Print();

		accountManagement.Print();

		System.out.print("----------- status end -------------\n");

		System.out.print("Execution finished!\n");
	}
}

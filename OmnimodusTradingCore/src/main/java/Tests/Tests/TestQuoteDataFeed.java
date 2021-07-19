package Tests.Tests;

import java.io.FileReader;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import java.util.*;

import org.jsoup.nodes.Document;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVParser;
import com.opencsv.CSVReader;

import DataLoader.CandleStick.CandleStick;
import DataFeed.Connector.ConnectorGeneric;
import DataFeed.QuoteDataFeed.QuoteDataFeed;

import Infrastructure.AccountManagement.*;
import Infrastructure.OrderManagement.*;
import Infrastructure.Quote.Quote;
import Infrastructure.Order.Order.*;

public class TestQuoteDataFeed {
	public static void BackTest() {
		// -------------- parameter definition ----------
		String underlying = "EURCHF";

		String fileID = "Test000001";
		String[] assetClasses = { "FX" };
		String[] symbols = {"BTCUSD"};
		QuoteDataFeed quoteDataFeed = new QuoteDataFeed(assetClasses, symbols, new ConnectorGeneric());


		int tickLimit = 1000;
		// -----------------------------

		double spread = 0.00000001;
		//CSVParser parser = new CSVParserBuilder().withSeparator(',').withIgnoreQuotations(true).build();

		String file = "E:/Projects/Algo Trading/Git/MonkeyTrading/HistoricalData/FX/minutely/BTCUSD1.csv"; // D:/MonkeyTrading/HistoricalData/Equity/minutely/test.csv
		int currentTick = 0;
		
		
		try {

			FileReader filereader = new FileReader(file);

			CSVReader csvReader = new CSVReader(filereader);
			String[] nextRecord;
			Quote[] quoteList;
			
			while ((nextRecord = csvReader.readNext()) != null) {
				if (currentTick >= tickLimit) {
					break;
				}
				for (Quote quote : CandleStick.CandleStickFromString(symbols[0], nextRecord).Expand(4, spread)) {

					System.out.print(quote.Print() + "\n");
					
					quoteDataFeed.RefreshStatic("FX", quote);
					
				}
				currentTick += 1;

				System.out.println();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.print("Error in Loop!");
		}
		
		System.out.print("Execution finished!");
	}
}

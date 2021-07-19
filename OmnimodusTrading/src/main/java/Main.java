package _Main;

import QuoteDataFeed.*;
import Tests.*;
import Tests.TestBackTester.*;
import org.jsoup.select.Elements;

import com.opencsv.CSVReader;

import CandleStick.CandleStick;
import Quote.*;
import Connector.*;
import java.io.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;



public class Main {

	public static void main(String[] args) {

	String Test = "BackTestDev";
	/*Test
	 * Quotes
	 * loadingCSV
	 * Calendar
	 * ZonedTime
	 * BackTestDev
	 */
	
	if (Test == "Quotes")
	{
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
		//-- 0 and 1: date and time
		//System.out.print("TEST\n");
		//System.out.print("FIRST: " + inputCSV[0]);
		//System.out.print("\nSECOND: " + inputCSV[1]);
		//System.out.print(inputCSV[0] + " " + inputCSV[1]);
		
		ZonedDateTime timeParsed = ZonedDateTime.parse("2020.07.06" + " " + "09:50", formatter.withZone(ZoneId.systemDefault()));
		
		ZoneId zoneId = ZoneId.of("UTC+1");;
		ZonedDateTime time = ZonedDateTime.of(2015, 11, 30, 23, 45, 59, 1234, zoneId);
		double bid = 1.1;
		double ask = 1.11;
		String[] assetClasses = {"FX"};
		String symbol = "EURCHF";
		
		QuoteDataFeed qdf = new QuoteDataFeed(assetClasses, new ConnectorGeneric());
		
		qdf.AddFirst("FX", symbol, time, bid, ask);
		
		QuoteFX q = (QuoteFX)qdf.GetQuote("FX", "EURCHF");
		
		System.out.print(q.GetAsk() + "\n");
		
		System.out.print(qdf.PrintForAssetClass("FX"));
		
	}
	
	else if (Test == "loadingCSV")
	{
		var file = "E:/Projects/Algo Trading/Git/MonkeyTrading/HistoricalData/FX/minutely/BTCUSD1.csv"; //  D:/MonkeyTrading/HistoricalData/Equity/minutely/test.csv
		try {
			  
	        FileReader filereader = new FileReader(file);
	  
	        CSVReader csvReader = new CSVReader(filereader);
	        String[] nextRecord;
	  
	        while ((nextRecord = csvReader.readNext()) != null) {
	            for (String cell : nextRecord) {
	                System.out.print(cell + "\t");
	            }
	            System.out.println();
	        }
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	    }
		
		}
	
	else if (Test == "Calendar") {
		TestCalendar newTest = new TestCalendar();
		newTest.createCSV(); //VIA CSV
		//newTest.eventspacked(); // Events without duplicates. Missing events due to overwriting
		//newTest.multiKeys(); // Events with duplicates
	}
	
	else if (Test == "ZonedTime") {
		TestCalendar newTest = new TestCalendar();
		newTest.zonedDateTimeTest("America/New_York");
	}
	
	else if (Test == "BackTestDev") 
	{
		TestBackTester.BackTest();
	}
	else {
		System.out.println("Dummy");
	}
	
	
	}

}

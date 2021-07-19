package DataFeed.CalendarFeed;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

import org.jsoup.nodes.Document;

public class ExportForexCalendar{

	
	
	 public void createCSV(LocalDate localDate) {
		 	ForexFactoryCalendarData FCD = new ForexFactoryCalendarData();
		 	LocalDate todayCalendar = LocalDate.now();
			Document fullHTMLPage = FCD.GetCalendarWebsite(todayCalendar);
			FCD.SetfullHTMLPage(fullHTMLPage);
			List<String> daysEvents = FCD.GetEventList();
			List<String> daysCurrencies = FCD.GetCurrenciesList(); 
			List<String> daysActuals = FCD.GetActualList();
			List<String> daysTime = FCD.GetTimeList();
		    	
		    PrintWriter pw = null;
		    try {
		        pw = new PrintWriter(new File("NewData.csv"));
		    } catch (FileNotFoundException e) {
		        e.printStackTrace();
		    }
		    StringBuilder builder = new StringBuilder();
		    String columnNamesList = "events, currencies, actual, time";
		    builder.append(columnNamesList +"\n");
		    for (int i = 0; i < daysTime.size(); i++) 
		    {
		    	builder.append(daysEvents.get(i) +", " );
		    	builder.append(daysCurrencies.get(i)+", ");
		    	builder.append(daysActuals.get(i)+", ");
		    	builder.append(daysTime.get(i)+", ");
		    	builder.append('\n');
		    }
		    // No need give the headers Like: id, Name on builder.append
		    /*builder.append(columnNamesList +"\n");
		    builder.append("1"+",");
		    builder.append("Chola");
		    builder.append('\n');
		    */
		    pw.write(builder.toString());
		    pw.close();
		    System.out.println("done!");
		    }
	
	
}

package CalendarFeed;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.jsoup.nodes.Document;

import com.google.common.collect.Multimap;

public class GetCalendarTimeSpan extends GetForexFactoryCalendarPage{
	
	private LocalDate startDate;
	private LocalDate endDate;
	
	//Getter
	public LocalDate GetStartTime(LocalDate startDate) {return this.startDate;}
	public LocalDate GetEndTime(LocalDate endDate) {return this.endDate;}
	
	//Setter
	public void SetStartTime(LocalDate startDate) {this.startDate = startDate;}
	public void SetEndTime(LocalDate endDate) {this.endDate = endDate;}
	
	//Further variables
	List<String> combinedEvents = new ArrayList<String>();
	List<String> combinedCurrencies = new ArrayList<String>();
	List<String> combinedTimes = new ArrayList<String>();
	List<String> combinedActuals = new ArrayList<String>();
	
	List<java.time.LocalDate> currentDate = new ArrayList<LocalDate>();
	//String startDate2 = "03-01-2021";
	//String endDate2 = "05-01-2021";
	
	//LocalDate startDate3 = LocalDate.parse(startDate2, formatter);;
	//LocalDate endDate3 = LocalDate.parse(endDate2, formatter);;
	
	public Multimap<String, List<Object>> getFullCalendar(){
		
		ForexFactoryCalendarData FFCEV = new ForexFactoryCalendarData();
		FFCEV.SetWebsite("https://www.forexfactory.com");
		
		for (LocalDate date = this.startDate; date.isBefore(this.endDate); date = date.plusDays(1)) {
			
			Document fullHTMLPage = FFCEV.GetCalendarWebsite(date);
			FFCEV.SetfullHTMLPage(fullHTMLPage);
			
			List<String> daysEvents = FFCEV.GetEventList();
			List<String> daysCurrencies = FFCEV.GetCurrenciesList(); 
			List<String> daysActuals = FFCEV.GetActualList();
			List<String> daysTime = FFCEV.GetTimeList();
		     
			combinedEvents.addAll(daysEvents);
			combinedCurrencies.addAll(daysCurrencies);
			combinedTimes.addAll(daysActuals);
			combinedActuals.addAll(daysTime);
			
			int numEventsAtDay = combinedEvents.size();
			for (int counter = 0; counter <= numEventsAtDay; counter++ ) {
				currentDate.add(date);
			}
			
		}
		System.out.println(combinedEvents);
		System.out.println(combinedCurrencies);
		System.out.println(combinedTimes);
		System.out.println(combinedActuals);
		System.out.println(currentDate);
		return null;
		
	};
	

}

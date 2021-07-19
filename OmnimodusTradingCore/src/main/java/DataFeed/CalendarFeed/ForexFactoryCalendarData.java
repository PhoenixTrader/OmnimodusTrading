package DataFeed.CalendarFeed;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class ForexFactoryCalendarData extends GetForexFactoryCalendarPage
{
	
	private Document fullHTMLPage;
	
	private LocalDate calendarDate;
	
	//Getter
	public Document GetfullHTMLPage(Document fullHTMLPage) {return this.fullHTMLPage;}

	public LocalDate GetzonedDate(LocalDate calendarDate) {return this.calendarDate;}

	//Setter
	public void SetfullHTMLPage(Document fullHTMLPage) {this.fullHTMLPage = fullHTMLPage;}

	public void SetzonedDate(LocalDate calendarDate) {this.calendarDate = calendarDate;}
	
	//Create lists
	public List<String> GetEventList() {      
		
	List<String> eventList = new ArrayList<String>();
		Elements events = this.fullHTMLPage.select("td.calendar__event");
		for (Element event : events){
			eventList.add(event.text());
	};
		return eventList;
	};
	
	public List<String> GetCurrenciesList() {		
		
		List<String> currencyList = new ArrayList<String>();
		Elements currencies = this.fullHTMLPage.select("td.calendar__currency");
		for (Element currency : currencies){
			currencyList.add(currency.text());
		}
		return currencyList;
	}
	
	public List<String> GetTimeList() {		
		
		List<String> timeList = new ArrayList<String>();
		Elements times = this.fullHTMLPage.select("td.calendar__time");
		for (Element time : times){
			String StringTime = time.text();
			StringTime = ConvertedTimeCalendar(StringTime);
			timeList.add(StringTime);
		}
			return timeList;
		};
	
		
    public List<ZonedDateTime> zonedDatetime(LocalDate localDate, List<String> listCalendarTime, String zoneID){
    	List<ZonedDateTime> calendarDateTimes = new ArrayList<ZonedDateTime>();	
    	ZonedDateTime calendarTime;
		for (int i = 0; i < listCalendarTime.size(); i++) {
			String checker = listCalendarTime.get(i);
			//System.out.print(checker);
			int checkerlength = checker.length();
			if (checkerlength == 8 ) {
			calendarTime = createZonedDateTime(localDate, listCalendarTime.get(i), zoneID);
			calendarDateTimes.add(calendarTime);
			}
			else if (checkerlength == 7)
			{
				calendarTime = createZonedDateTime(localDate, "00:00:00", zoneID);
				calendarDateTimes.add(calendarTime);
			} 
			else {
				int previousCounter = i - 1;
				calendarTime = createZonedDateTime(localDate, listCalendarTime.get(previousCounter), zoneID);
				calendarDateTimes.add(calendarTime);
			}
		}
    	return calendarDateTimes;
	    }
	    
	public ZonedDateTime createZonedDateTime(LocalDate localDate, String time, String zoneID) {
		if (time == "All Day") {
			time = "00:00:00";
		}
		LocalDateTime localDateTime = localDate.atTime(Integer.parseInt(time.substring(0,2)), Integer.parseInt(time.substring(3,5))
				, Integer.parseInt(time.substring(6,8))); 
		ZoneId zoneId = ZoneId.of(zoneID); // Zone information: e.g. "Asia/Kolkata", "America/New_York"
		ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId); // add zone information
		return zonedDateTime;
	}
		
	public List<String> GetActualList() {
		
		List<String> actualList = new ArrayList<String>();
		Elements actuals = this.fullHTMLPage.select("td.calendar__actual");
		for (Element actual : actuals){
			actualList.add(actual.text());
		};
		return actualList;
	};
	
	public List<String> GetImpactList() {	
	
		List<String> impactList = new ArrayList<String>();
		Elements impacts = this.fullHTMLPage.select("span");
		for (Element impact : impacts){
			impactList.add(impact.text());
	};
		return impactList;
	}	
	
	private Hashtable<String, String> afternoonDictionary() {
		// creating a My HashTable Dictionary
		Hashtable<String, String> afternoonHours = new Hashtable<String, String>();
		afternoonHours.put("1", "13");
		afternoonHours.put("2", "14");
		afternoonHours.put("3", "15");
		afternoonHours.put("4", "16");
		afternoonHours.put("5", "17");
		afternoonHours.put("6", "18");
		afternoonHours.put("7", "19");
		afternoonHours.put("8", "20");
		afternoonHours.put("9", "21");
		afternoonHours.put("10", "22");
		afternoonHours.put("11", "23");
		afternoonHours.put("12", "00");
		return afternoonHours;
	}
	
	public String ConvertedTimeCalendar(String calendarTime){ 

		Hashtable<String, String> afternoonHours = afternoonDictionary();
		
			  if (calendarTime.indexOf("Day") !=-1) {
				  return "00:00:01"; 
			  }
			  else if (calendarTime.indexOf("pm") != -1) {
				  String cleanedTime = calendarTime.replace("pm", "").strip();
				  String[] timeParts = cleanedTime.split(":");
				  String hour = timeParts[0];
				  String minutes = timeParts[1];
				  hour = afternoonHours.get(hour);
				  String completeTime = hour + ":" +  minutes + ":00";
				  return  completeTime;				  
			  }
			  else if (calendarTime.indexOf("am") != -1) {
				  String cleanedTime = calendarTime.replace("am", "").strip();
				  String[] timeParts = cleanedTime.split(":");
				  String hour = timeParts[0];
				  String minutes = timeParts[1];
				  if (hour.length() == 1) {
					  hour = "0" + hour;
				  }
				  String completeTime = hour + ":" +  minutes + ":00";
				  return  completeTime;
			  }
			  else {
				  return calendarTime;
			  }	   
	}
	
	//Getter methods for calendar data 
	private List<String> events()
	{ 
		return GetEventList();
	}
	
	private List<String> currencies() 
	{
		return GetCurrenciesList();	
	}
	
	private List<String> actuals() 
	{
		return GetActualList();	
	}
	
	private List<ZonedDateTime> times()
	{
		List<String> GetDateTimeAsStrings;
		List<ZonedDateTime> GetDateTimeAsZonedDateTime;
		GetDateTimeAsStrings = GetTimeList();
		GetDateTimeAsZonedDateTime = zonedDatetime(this.calendarDate, GetDateTimeAsStrings, "America/New_York");
		return GetDateTimeAsZonedDateTime;
	}
		
	//Methods supporting search
	public int getIndexCalendarStrings(String ItemToSearch) {
		int ItemPosition = events().indexOf(ItemToSearch);
		return ItemPosition;
	}
	
	public int getIndexZonedDateTime(ZonedDateTime itemToSearch) {
		int ItemPosition = times().indexOf(itemToSearch);
		return ItemPosition;
	}
	
	//Search methods to get values
	public Multimap<String, List<Object>> Searchevent(String ItemToSearch)
	{
		int position = getIndexCalendarStrings(ItemToSearch);
		String event = events().get(position);
		String currency = currencies().get(position);
		ZonedDateTime time = times().get(position);
		String actual = actuals().get(position);
		ArrayList<Object> eventData = new ArrayList <Object>();
	    eventData.add(currency);
	    eventData.add(time);
	    eventData.add(actual);
	    Multimap<String, List<Object>> eventDictionary = ArrayListMultimap.create();
	    eventDictionary.put(event, eventData);	
		return eventDictionary;
	}
	
	public Multimap<ZonedDateTime, List<Object>> SearchDateTime(ZonedDateTime ItemToSearch)
	{
		int position = getIndexZonedDateTime(ItemToSearch);
		//TODO - Try Catch of time is not found
		try {
			String event = events().get(position);
			String currency = currencies().get(position);
			ZonedDateTime time = times().get(position);
			String actual = actuals().get(position);
		    
			//ArrayList<String> eventData = new ArrayList<String>();
			ArrayList<Object> timeData = new ArrayList <Object>();
			timeData.add(event);
			timeData.add(currency);
			timeData.add(actual);
		    Multimap<ZonedDateTime, List<Object>> eventDictionary = ArrayListMultimap.create();
		    eventDictionary.put(time, timeData);
				
			return eventDictionary;
		}
		catch(Exception e) {
		    Multimap<ZonedDateTime, List<Object>> eventDictionary = ArrayListMultimap.create();
		    return eventDictionary;
		}
	}
	
	public Multimap<String, List<Object>> SearchCurrency(String ItemToSearch)
	{
		int position = getIndexCalendarStrings(ItemToSearch);
		String event = events().get(position);
		String currency = currencies().get(position);
		ZonedDateTime time = times().get(position);
		String actual = actuals().get(position);
		ArrayList<Object> eventData = new ArrayList <Object>();
	    eventData.add(event);
	    eventData.add(time);
	    eventData.add(actual);
	    Multimap<String, List<Object>> eventDictionary = ArrayListMultimap.create();
	    eventDictionary.put(currency, eventData);
			
		return eventDictionary;
	}
	
	public Multimap<String, List<Object>> SearchActual(String ItemToSearch)
	{
		int position = getIndexCalendarStrings(ItemToSearch);
		String event = events().get(position);
		String currency = currencies().get(position);
		ZonedDateTime time = times().get(position);
		String actual = actuals().get(position);
		ArrayList<Object> eventData = new ArrayList <Object>();
	    eventData.add(event);
	    eventData.add(currency);
	    eventData.add(time);
	    Multimap<String, List<Object>> eventDictionary = ArrayListMultimap.create();
	    eventDictionary.put(actual, eventData);
		return eventDictionary;
	}
	
	//
	
	
	
}

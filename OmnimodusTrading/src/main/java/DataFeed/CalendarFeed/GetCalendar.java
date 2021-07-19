package DataFeed.CalendarFeed;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.script.ScriptException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import DataFeed.WebScraper.*;

public class GetCalendar {
	
	
	//----- Variables
	
	private String website;
	private String monthString;
	
	
	private String[] monthList = { "jan",      
								   "feb",
								   "mar",        
								   "apr",        
								   "may",          
								   "jun",         
								   "jul",         
								   "aug",       
								   "sep",    
								   "oct",      
								   "nov",     
								   "dec" };

	
	private Hashtable<String, String> monthToString() {
		// creating a My HashTable Dictionary
		Hashtable<String, String> monthConverter = new Hashtable<String, String>();
		monthConverter.put("1", "jan");
		monthConverter.put("2", "feb");
		monthConverter.put("3", "mar");
		monthConverter.put("4", "apr");
		monthConverter.put("5", "may");
		monthConverter.put("6", "jun");
		monthConverter.put("7", "jul");
		monthConverter.put("8", "aug");
		monthConverter.put("9", "sep");
		monthConverter.put("10", "oct");
		monthConverter.put("11", "nov");
		monthConverter.put("12", "dec");
		return monthConverter;
	}
	
	
	//----- Get functions
	public String GetWebsite(String website) {return this.website;}
	
	//----- Setter
    public void SetWebsite(String website) {this.website = website;}
	
	//----- Methods
	
	public String GetCalendarDate(String dayAsString, int monthAsInteger, String yearAsString) {
		String monthAsString = Integer.toString(monthAsInteger);
		if (monthAsString.length() == 1) {
			monthAsString = "0" + monthAsString;
		}
		String calendarDate = dayAsString + "-" + monthAsString + "-" + yearAsString;
		return calendarDate;
		//"dd-MM-yyyy"
	}
    
	public String GetTodaysEventWebsiteName() {
		    website = GetWebsite(this.website);
			String fullWebsite = website + "/calendar?day=today";
			return fullWebsite;
	}
	
	public String GetThisWeeksEventWebsiteName() {
		    website = GetWebsite(this.website);
			String fullWebsite = website + "/calendar?week=this";
			return fullWebsite;
	}
    
	public String GetFullWebsiteNameBasedOnDate(LocalDate date) {
		//Parameters type are choosen based on the need of the webpage requirement
			Hashtable<String, String> monthToString = monthToString();
			
			String dayAsString = Integer.toString(date.getDayOfMonth());
			String monthString = Integer.toString(date.getMonthValue());
			String yearAsString = Integer.toString(date.getYear());
		    monthString = monthToString.get(monthString);
		    website = GetWebsite(this.website);
			String fullWebsite = website + "/calendar?day=" + monthString + dayAsString+ "." + yearAsString;
			return fullWebsite;
	}
	
	public String GetFullWebsiteNameBasedOnDateLEGACY(String dayAsString, int monthAsInteger, String yearAsString) {
		//Parameters type are choosen based on the need of the webpage requirement
		    monthString = monthList[monthAsInteger-1];
		    website = GetWebsite(this.website);
			String fullWebsite = website + "/calendar?day=" + monthString + dayAsString+ "." + yearAsString;
			return fullWebsite;
	}
		
	public Document GetCalendarWebsite(String website) {
		
		CHttpRequester requester = new CHttpRequester();
		Document webSiteContent = null;
		try {
			webSiteContent = requester.get(website);
			//htmlPage = webSiteContent.select("tr");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return webSiteContent;
	}
		
	public List<String> GetEventList(Document fullHTMLPage) {      
		
		List<String> eventList = new ArrayList<String>();
		Elements events = fullHTMLPage.select("td.calendar__event");
		for (Element event : events){
			eventList.add(event.text());
	};
		return eventList;
	};
	
	public List<String> GetCurrenciesList(Document websiteData) {		
		
		List<String> currencyList = new ArrayList<String>();
		Elements currencies = websiteData.select("td.calendar__currency");
		for (Element currency : currencies){
			currencyList.add(currency.text());
		}
		return currencyList;
	}
	
	public List<String> GetTimeList(Document websiteData) {		
		
		List<String> timeList = new ArrayList<String>();
		Elements times = websiteData.select("td.calendar__time");
		for (Element time : times){
			String StringTime = time.text();
			StringTime = ConvertedTimeCalendar(StringTime);
			timeList.add(StringTime);
		}
			return timeList;
		};
	
	public List<String> GetActualList(Document websiteData) {
		
		List<String> actualList = new ArrayList<String>();
		Elements actuals = websiteData.select("td.calendar__actual");
		for (Element actual : actuals){
			actualList.add(actual.text());
		};
		return actualList;
	};
	
	public List<String> GetImpactList(Document websiteData) {	
	
		List<String> impactList = new ArrayList<String>();
		Elements impacts = websiteData.select("span");
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
		
			  if (calendarTime == "Day") {
				  return "00:00:00"; 
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
	
	public ZonedDateTime createZonedDateTime(LocalDate localDate, String time, String zoneID) {
		//Create TimeKey for Hashtable
		LocalDateTime localDateTime = localDate.atTime(Integer.parseInt(time.substring(0,2)), Integer.parseInt(time.substring(3,5))
				, Integer.parseInt(time.substring(6,8)));  //Add time information
		//System.out.println(localDateTime);
		ZoneId zoneId = ZoneId.of(zoneID); // Zone information: e.g. "Asia/Kolkata", "America/New_York"
		ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId); // add zone information
		return zonedDateTime;
		
	}
	
	public String createcalendarDateTime(LocalDate localDate, String time) {
		//Create TimeKey for Hashtable
		LocalDateTime localDateTime = localDate.atTime(Integer.parseInt(time.substring(0,2)), Integer.parseInt(time.substring(3,5))
				, Integer.parseInt(time.substring(6,8)));  //Add time information
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
	    String calendarDateTime = localDateTime.format(formatter); 
		return calendarDateTime;
	}
	
	public Hashtable<String, List<String>> eventsList(LocalDate localDate)
	{

		String todayCalendar = GetFullWebsiteNameBasedOnDate(localDate);
		Document fullHTMLPage = GetCalendarWebsite(todayCalendar);
		List<String> daysEvents = GetEventList(fullHTMLPage);
		List<String> daysCurrencies = GetCurrenciesList(fullHTMLPage); 
		List<String> daysActuals = GetActualList(fullHTMLPage);
		List<String> daysTime = GetTimeList(fullHTMLPage);
		List<String> calendarDateTimes = new ArrayList<String>();
		String calendarTime;
		for (int i = 0; i < daysTime.size(); i++) {
			String checker = daysTime.get(i);
			int checkerlength = checker.length();
			if (checkerlength == 8) {
			calendarTime = createcalendarDateTime(localDate, daysTime.get(i));
			calendarDateTimes.add(calendarTime);
			}
			else {
				int previousCounter = i - 1;
				calendarTime = createcalendarDateTime(localDate, daysTime.get(previousCounter));
				calendarDateTimes.add(calendarTime);
			}
		}
		Hashtable<String, List<String>> todaysEvents = new Hashtable<String, List<String>>();
	    todaysEvents = todaysEvents(daysEvents.size(), daysEvents, daysCurrencies, calendarDateTimes, daysActuals);
	    return todaysEvents;	
	    
	}
	
	
	public Hashtable<String, List<String>> todaysEvents(Integer counter, List<String> events, List<String> currencies,  List<String> times, List<String> actuals) {
	  Hashtable<String, List<String>> eventDictionary = new Hashtable<String, List<String>>();
	  for (int i = 0; i < counter; i++) 
	      {
		  List<String> eventDataForDictionary = eventData(i, times, currencies, actuals);
		  eventDictionary.put(events.get(i), eventDataForDictionary);
	  	  }
	  return eventDictionary;
	}
	
	public List<String> eventData(Integer counter, List<String> events, List<String> currencies, List<String> actuals)
	{	
	  ArrayList<String> eventData = new ArrayList<String>();
  	  eventData.add(currencies.get(counter));
  	  eventData.add(events.get(counter));
  	  eventData.add(actuals.get(counter));
	return eventData;
	}

	
	/*
	 * MultiKey Testpart
	 * 
	 */
	
	public Multimap<String, List<String>> eventsListMulti(LocalDate localDate)
	{

		String todayCalendar = GetFullWebsiteNameBasedOnDate(localDate);
		Document fullHTMLPage = GetCalendarWebsite(todayCalendar);
		List<String> daysEvents = GetEventList(fullHTMLPage);
		List<String> daysCurrencies = GetCurrenciesList(fullHTMLPage); 
		List<String> daysActuals = GetActualList(fullHTMLPage);
		List<String> daysTime = GetTimeList(fullHTMLPage);
		List<String> calendarDateTimes = new ArrayList<String>();
		String calendarTime;
		for (int i = 0; i < daysTime.size(); i++) {
			String checker = daysTime.get(i);
			int checkerlength = checker.length();
			if (checkerlength == 8) {
			calendarTime = createcalendarDateTime(localDate, daysTime.get(i));
			calendarDateTimes.add(calendarTime);
			}
			else {
				int previousCounter = i - 1;
				calendarTime = createcalendarDateTime(localDate, daysTime.get(previousCounter));
				calendarDateTimes.add(calendarTime);
			}
		}
		Multimap<String, List<String>> eventDictionary = ArrayListMultimap.create();
		eventDictionary = TestMultimap(daysEvents.size(), daysEvents, daysCurrencies, calendarDateTimes, daysActuals);
	    return eventDictionary;	
	    
	}
	
	public Multimap<String, List<String>> TestMultimap(Integer counter, List<String> events, List<String> currencies,  List<String> times, List<String> actuals) {		
		Multimap<String, List<String>> eventDictionary = ArrayListMultimap.create();
		  for (int i = 0; i < counter; i++) 
	      {
		  List<String> eventDataForDictionary = eventData(i, events, currencies, actuals);
		  eventDictionary.put(times.get(i), eventDataForDictionary);
	  	  }
		return eventDictionary;
	}
		  /*
		Multimap<Integer, String> multimap = ArrayListMultimap.create();
		multimap.put(1, "A");
		multimap.put(1, "B");
		multimap.put(1, "C");
		multimap.put(1, "A");

		multimap.put(2, "A");
		multimap.put(2, "B");
		multimap.put(2, "C");

		multimap.put(3, "A");

		System.out.println(multimap.get(1));
		System.out.println(multimap.get(2));       
		System.out.println(multimap.get(3));
		*/
	    public void createCSV(LocalDate localDate) {
		String todayCalendar = GetFullWebsiteNameBasedOnDate(localDate);
		Document fullHTMLPage = GetCalendarWebsite(todayCalendar);
		List<String> daysEvents = GetEventList(fullHTMLPage);
		List<String> daysCurrencies = GetCurrenciesList(fullHTMLPage); 
		List<String> daysActuals = GetActualList(fullHTMLPage);
		List<String> daysTime = GetTimeList(fullHTMLPage);
	    	
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

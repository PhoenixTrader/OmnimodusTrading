package Tests;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Hashtable;
import java.util.List;
import org.jsoup.nodes.Document;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import CalendarFeed.ForexFactoryCalendarData;
import CalendarFeed.GetCalendar;
import CalendarFeed.GetCalendarTimeSpan;
import CalendarFeed.GetForexFactoryCalendarPage;

public class TestCalendar {

	public void dateTest()
	{
	
	String TESTTIME = "9:00am";
	GetCalendar calendarfeed = new GetCalendar();
	TESTTIME = calendarfeed.ConvertedTimeCalendar(TESTTIME);
	String calDateTest = calendarfeed.GetCalendarDate("22", 3, "2021");
	//System.out.println(TESTTIME);
	System.out.println(calDateTest);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    LocalDate localDate = LocalDate.parse(calDateTest, formatter);
    System.out.println("localDate: " + localDate); 
    
    calendarfeed.SetWebsite("https://www.forexfactory.com");
    String testContent = calendarfeed.GetFullWebsiteNameBasedOnDate(localDate);
    System.out.println(testContent);
	}
	
	public void zonedDateTimeTest(String zoneID)
	{
	
	String TESTTIME = "09:00:00";
	GetCalendar calendarfeed = new GetCalendar();
	TESTTIME = calendarfeed.ConvertedTimeCalendar(TESTTIME);
	String calDateTest = calendarfeed.GetCalendarDate("22", 3, "2021");
	//System.out.println(TESTTIME);

	System.out.println(calDateTest);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    LocalDate localDate = LocalDate.parse(calDateTest, formatter);
    System.out.println("localDate: " + localDate);
    
	ZonedDateTime testZonedTime = calendarfeed.createZonedDateTime(localDate, TESTTIME, zoneID); 
	ZonedDateTime testZonedTime2 = calendarfeed.createZonedDateTime(localDate, TESTTIME, "Asia/Kolkata");
    System.out.println(testZonedTime);
    System.out.println(testZonedTime2);
	}
	
	public void eventsunpacked() {
		GetCalendar calendarfeed = new GetCalendar();
        calendarfeed.SetWebsite("https://www.forexfactory.com");
        String calDateTest = calendarfeed.GetCalendarDate("22", 3, "2021");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate localDate = LocalDate.parse(calDateTest, formatter);
		String todayCalendar = calendarfeed.GetFullWebsiteNameBasedOnDate(localDate);
		Document fullHTMLPage = calendarfeed.GetCalendarWebsite(todayCalendar);
		List<String> daysEvents = calendarfeed.GetEventList(fullHTMLPage);
		List<String> daysCurrencies = calendarfeed.GetCurrenciesList(fullHTMLPage); 
		List<String> daysActuals = calendarfeed.GetActualList(fullHTMLPage);
		List<String> daysTime = calendarfeed.GetTimeList(fullHTMLPage);
	     
		Hashtable<String, List<String>> todaysEvents = new Hashtable<String, List<String>>();
	    todaysEvents = calendarfeed.todaysEvents(daysEvents.size(), daysEvents, daysCurrencies, daysTime, daysActuals);
	    System.out.println(todaysEvents);
	}
	
	public void eventspacked() {
		GetCalendar calendarfeed = new GetCalendar();
        calendarfeed.SetWebsite("https://www.forexfactory.com");
        String calDateTest = calendarfeed.GetCalendarDate("04", 1, "2021");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate localDate = LocalDate.parse(calDateTest, formatter);
        System.out.println(localDate);
        Hashtable<String, List<String>> todaysEvents = calendarfeed.eventsList(localDate);
        System.out.println(todaysEvents);
	}
	
	public void multiKeys() {
		GetCalendar calendarfeed = new GetCalendar();
        calendarfeed.SetWebsite("https://www.forexfactory.com");
        String calDateTest = calendarfeed.GetCalendarDate("11", 1, "2021");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate localDate = LocalDate.parse(calDateTest, formatter);
        Multimap<String, List<String>> todaysEvents = calendarfeed.eventsListMulti(localDate);
        //System.out.println(todaysEvents);
        System.out.println(todaysEvents.get("22-03-2021 22:00:00"));
	}
	
	public void createCSV() {
		GetCalendar calendarfeed = new GetCalendar();
        calendarfeed.SetWebsite("https://www.forexfactory.com");
        String calDateTest = calendarfeed.GetCalendarDate("22", 3, "2021");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate localDate = LocalDate.parse(calDateTest, formatter);
		calendarfeed.createCSV(localDate);
	}
	
	
	public void testCalendarSearcher() {
		GetCalendar calendarfeed = new GetCalendar();
		String calDateTest = calendarfeed.GetCalendarDate("04", 12, "2020");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate localDate = LocalDate.parse(calDateTest, formatter);
		GetForexFactoryCalendarPage GFCP = new GetForexFactoryCalendarPage();
		GFCP.SetWebsite("https://www.forexfactory.com");
		Document htmlPage = GFCP.GetCalendarWebsite(localDate);
		ForexFactoryCalendarData FFCD = new ForexFactoryCalendarData();
		FFCD.SetfullHTMLPage(htmlPage);
		FFCD.SetzonedDate(localDate);
		List<String> Testevents = FFCD.GetEventList();
		System.out.println(Testevents);
		
		//Create CalendarData
		//ForexFactoryCalendarData newCalendarData = new ForexFactoryCalendarData();
		//newCalendarData.SetEvents(daysEvents);
		//newCalendarData.SetCurrencies(daysCurrencies);
		//newCalendarData.SetTimes(daysTime);
		//newCalendarData.SetActuals(daysActuals);
		//System.out.println(FFCD.Searchevent("Credit Card Spending y/y"));
	}
	
	
	public void testPureWebsite(){
		
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		String today = dtf.format(now);
		
		GetForexFactoryCalendarPage GetForexFactoryCalendarPage = new GetForexFactoryCalendarPage();
		GetForexFactoryCalendarPage.SetWebsite("https://www.forexfactory.com");
        String calDateTest = GetForexFactoryCalendarPage.GetCalendarDate("22", 3, "2021");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate localDate = LocalDate.parse(calDateTest, formatter);
		Document fullHTMLPage = GetForexFactoryCalendarPage.GetCalendarWebsite(localDate);
		System.out.print(fullHTMLPage);
	}
	
	
	public void doubleClassCalendar(){
		
		ForexFactoryCalendarData FFCEV = new ForexFactoryCalendarData();
		FFCEV.SetWebsite("https://www.forexfactory.com");
		String calDateTest = FFCEV.GetCalendarDate("22", 3, "2021");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate localDate = LocalDate.parse(calDateTest, formatter);
		Document fullHTMLPage = FFCEV.GetCalendarWebsite(localDate);
		FFCEV.SetfullHTMLPage(fullHTMLPage);
		FFCEV.SetzonedDate(localDate);
		ZonedDateTime  time = ZonedDateTime.parse("2021-03-22T07:00-04:00[America/New_York]");
		Multimap<ZonedDateTime, List<Object>> SearchDateTime = FFCEV.SearchDateTime(time);
		try {
			List<Object> singleListe = Iterables.get(SearchDateTime.get(time), 0);
			boolean ans = singleListe.get(0) != null;
			if (ans == true) {
			}
			else {
				System.out.print("Is empty");
			}
		}
		catch(Exception e) {
			System.out.print("Is empty");
		}
	}
	
	public void downloadTimeSpan() {
		
		GetCalendarTimeSpan downloadCalendar = new GetCalendarTimeSpan();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		String startDateTest = "03-01-2021";
		String endDateTest = "05-01-2021";
		LocalDate startDate = LocalDate.parse(startDateTest, formatter);;
		LocalDate endDate = LocalDate.parse(endDateTest, formatter);;
		downloadCalendar.SetStartTime(startDate);
		downloadCalendar.SetEndTime(endDate);
		downloadCalendar.getFullCalendar();
		
	}
	
}

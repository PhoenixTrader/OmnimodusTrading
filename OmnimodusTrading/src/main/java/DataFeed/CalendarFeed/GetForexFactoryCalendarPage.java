package CalendarFeed;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Hashtable;

import javax.script.ScriptException;

import org.jsoup.nodes.Document;

import WebScraper.CHttpRequester;

public class GetForexFactoryCalendarPage {

	//----- Variables
	
	private String website;
	
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
	
	public Document GetCalendarWebsite(LocalDate date) {
		
		String website = GetFullWebsiteNameBasedOnDate(date);
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
	
}

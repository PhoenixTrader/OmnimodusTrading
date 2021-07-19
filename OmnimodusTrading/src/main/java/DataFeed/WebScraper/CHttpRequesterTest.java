package WebScraper;


public class CHttpRequesterTest {

	public static void main(String args[]) throws Exception {
		
	CHttpRequester requester = new CHttpRequester();
	requester.get("https://www.forexfactory.com/calendar?day=today");
	
}

}

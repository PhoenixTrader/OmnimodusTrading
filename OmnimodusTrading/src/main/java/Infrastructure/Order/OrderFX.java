package Order;
import java.time.LocalDateTime;
import QuoteDataFeed.*;
import Quote.*;

public class OrderFX extends Order
{
	

	        
	public OrderFX(String action, String buySell, double size, String assetClass, String underlying, 
            QuoteDataFeed quoteDataFeed, 
            double limit, double takeProfit, double stopLoss, 
            int identifier1, int identifier2, String comment) 
	{
		super(action, buySell, size, assetClass, underlying, quoteDataFeed, limit, takeProfit, stopLoss, identifier1, identifier2, comment);
		
	}
}



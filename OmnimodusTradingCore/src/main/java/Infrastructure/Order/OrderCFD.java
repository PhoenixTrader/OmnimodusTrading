package Infrastructure.Order;
import java.time.LocalDateTime;
import DataFeed.QuoteDataFeed.*;
import Infrastructure.Quote.*;

public class OrderCFD extends Order
{
	public OrderCFD(String action, String buySell, double size, String assetClass, String underlying, 
            QuoteDataFeed quoteDataFeed, 
            double limit, double takeProfit, double stopLoss, 
            int identifier1, int identifier2, String comment) 
	{
		super(action, buySell, size, assetClass, underlying, quoteDataFeed, limit, takeProfit, stopLoss, identifier1, identifier2, comment);
		
	};
}

package server.discountstrategies;

import server.OrderDetails;

public interface StrategyDiscount {

    double calculatePriceAfterDiscount(OrderDetails orderDetails);

    String getDescription();
}

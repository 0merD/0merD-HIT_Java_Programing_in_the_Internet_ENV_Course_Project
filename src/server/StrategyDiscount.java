package server;

public interface StrategyDiscount {

    double calculatePriceAfterDiscount(OrderDetails orderDetails);

    String getDescription();
}

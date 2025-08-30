package server;

public interface StrategyDiscount {

    double applyDiscount(OrderDetails orderDetails);

    String getDescription();
}

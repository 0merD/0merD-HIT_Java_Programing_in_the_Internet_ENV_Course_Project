package server;

import java.util.HashMap;
import java.util.Map;

public class CustomerDiscountsRegistry {
    private final Map<String, StrategyDiscount> strategies = new HashMap<>();

    public void addStrategy(String name, StrategyDiscount strategy) {
        strategies.put(name, strategy);
    }

    public StrategyDiscount getBestStrategy(OrderDetails orderDetails) {
        double originalPrice = orderDetails.getTotalPrice();
        StrategyDiscount bestStrategy = null;
        double maxDiscount = 0.0;

        for (StrategyDiscount strategy : strategies.values()) {
            double finalPrice = strategy.calculatePriceAfterDiscount(orderDetails);
            double discountAmount = originalPrice - finalPrice;
            if (discountAmount > maxDiscount) {
                maxDiscount = discountAmount;
                bestStrategy = strategy;
            }
        }

        return bestStrategy != null ? bestStrategy : new DiscountStrategyNoDiscount();
    }

    public double getBestDiscountedPrice(OrderDetails orderDetails) {
        return getBestStrategy(orderDetails).calculatePriceAfterDiscount(orderDetails);
    }
}

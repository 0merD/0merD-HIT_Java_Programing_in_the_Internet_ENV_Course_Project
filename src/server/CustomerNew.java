package server;

import java.util.HashMap;
import java.util.Map;

public class CustomerNew extends CustomerAbstract{

    private final Map<String, StrategyDiscount> availableStrategies;

    public CustomerNew(String fullName, String idNumber, String phoneNumber, CustomerTypeEnum customerType) {
        super(fullName, idNumber, phoneNumber, customerType);

        availableStrategies = initializeStrategies();
    }

    @Override
    public double applyBestDiscount(OrderDetails orderDetails) {
        return 0;
    }

    private Map<String, StrategyDiscount> initializeStrategies() {
        Map<String, StrategyDiscount> strategies = new HashMap<>();

        strategies.put("No Discount", new DiscountStrategyNoDiscount());

        HashMap<Integer, Double> newCustomerQuantityThresholds = new HashMap<>();
        newCustomerQuantityThresholds.put(2, 0.05);
        newCustomerQuantityThresholds.put(5, 0.10);

        strategies.put("Quantity", new DiscountStrategyQuantity(newCustomerQuantityThresholds));

        return strategies;
    }

    public double calculateDiscountedPrice(OrderDetails orderDetails) {
        return getBestDiscountStrategy(orderDetails).calculatePriceAfterDiscount(orderDetails);
    }

    private StrategyDiscount getBestDiscountStrategy(OrderDetails orderDetails) {
        double originalPrice = orderDetails.getTotalPrice();
        StrategyDiscount bestStrategy = new DiscountStrategyNoDiscount(); // fallback
        double maxDiscount = 0.0;

        for (StrategyDiscount strategy : availableStrategies.values()) {
            double finalPrice = strategy.calculatePriceAfterDiscount(orderDetails);
            double discountAmount = originalPrice - finalPrice;

            if (discountAmount > maxDiscount) {
                maxDiscount = discountAmount;
                bestStrategy = strategy;
            }
        }

        return bestStrategy;
    }
}

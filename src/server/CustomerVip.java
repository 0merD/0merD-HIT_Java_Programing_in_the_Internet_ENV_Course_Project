package server;

import java.util.HashMap;
import java.util.Map;

public class CustomerVip extends CustomerAbstract {

    private final Map<String, StrategyDiscount> availableStrategies;

    public CustomerVip(String fullName, String idNumber, String phoneNumber, CustomerTypeEnum clientType) {
        super(fullName, idNumber, phoneNumber, clientType);

        availableStrategies = initializeVipStrategies();
    }


    private Map<String, StrategyDiscount> initializeVipStrategies() {
        Map<String, StrategyDiscount> strategies = new HashMap<>();

        strategies.put("No Discount", new DiscountStrategyNoDiscount());

        strategies.put("PercentageDiscount", new DiscountStrategyPercentage(0.2));

        HashMap<Integer, Double> returningCustomerQuantityThresholds = new HashMap<>();
        returningCustomerQuantityThresholds.put(2, 0.10);
        returningCustomerQuantityThresholds.put(5, 0.15);
        returningCustomerQuantityThresholds.put(10, 0.20);
        strategies.put("Quantity", new DiscountStrategyQuantity(returningCustomerQuantityThresholds));

        return strategies;
    }

    @Override
    public double applyBestDiscount(OrderDetails orderDetails) {
        return calculateDiscountedPrice(orderDetails);
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

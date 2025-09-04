package server;

import java.util.HashMap;
import java.util.Map;

public class CustomerReturning extends CustomerAbstract{

    private final Map<String, StrategyDiscount> availableStrategies;

    public CustomerReturning(String fullName, String idNumber, String phoneNumber, CustomerTypeEnum clientType) {
        super(fullName, idNumber, phoneNumber, clientType);

        availableStrategies = initializeStrategies();
    }

    @Override
    public double applyBestDiscount(OrderDetails orderDetails) {
        return 0;
    }

    private Map<String, StrategyDiscount> initializeStrategies() {

        Map<String, StrategyDiscount> strategies = new HashMap<>();

        strategies.put("No Discount", new DiscountStrategyNoDiscount());

        strategies.put("PercentageDiscount", new DiscountStrategyPercentage(0.1));

        HashMap<Integer, Double> returningCustomerQuantityThresholds = new HashMap<>();
        returningCustomerQuantityThresholds.put(2, 0.10);
        returningCustomerQuantityThresholds.put(5, 0.15);
        returningCustomerQuantityThresholds.put(10, 0.20);
        strategies.put("Quantity", new DiscountStrategyQuantity(returningCustomerQuantityThresholds));

        return strategies;
    }
}

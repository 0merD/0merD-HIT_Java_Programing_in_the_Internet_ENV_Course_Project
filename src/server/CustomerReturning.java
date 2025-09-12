package server;

import server.enums.CustomerTypeEnum;

import java.util.HashMap;

public class CustomerReturning extends CustomerAbstract{

    public CustomerReturning(String fullName, String idNumber, String phoneNumber, CustomerTypeEnum clientType, double totalSpent) {
        super(fullName, idNumber, phoneNumber, clientType, totalSpent);

        CustomerDiscountsRegistry registry = getCustomerDiscountsRegistry();

        registry.addStrategy("No Discount", new DiscountStrategyNoDiscount());

        // Optional percentage discount
        registry.addStrategy("PercentageDiscount", new DiscountStrategyPercentage(0.1));

        // Quantity-based discount thresholds
        HashMap<Integer, Double> returningCustomerQuantityThresholds = new HashMap<>();
        returningCustomerQuantityThresholds.put(2, 0.10);
        returningCustomerQuantityThresholds.put(5, 0.15);
        returningCustomerQuantityThresholds.put(10, 0.20);
        registry.addStrategy("Quantity", new DiscountStrategyQuantity(returningCustomerQuantityThresholds));
    }

    @Override
    public double applyBestDiscount(OrderDetails orderDetails) {
        return getCustomerDiscountsRegistry().getBestDiscountedPrice(orderDetails);
    }
}

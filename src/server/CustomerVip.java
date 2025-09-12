package server;

import server.enums.CustomerTypeEnum;

import java.util.HashMap;

public class CustomerVip extends CustomerAbstract {
    public CustomerVip(String fullName, String idNumber, String phoneNumber, CustomerTypeEnum clientType, double totalSpent) {
        super(fullName, idNumber, phoneNumber, clientType, totalSpent);

        CustomerDiscountsRegistry registry = getCustomerDiscountsRegistry();

        registry.addStrategy("No Discount", new DiscountStrategyNoDiscount());
        registry.addStrategy("PercentageDiscount", new DiscountStrategyPercentage(0.2));

        HashMap<Integer, Double> vipQuantityThresholds = new HashMap<>();
        vipQuantityThresholds.put(2, 0.10);
        vipQuantityThresholds.put(5, 0.15);
        vipQuantityThresholds.put(10, 0.20);
        registry.addStrategy("Quantity", new DiscountStrategyQuantity(vipQuantityThresholds));
    }

    @Override
    public double applyBestDiscount(OrderDetails orderDetails) {
        return getCustomerDiscountsRegistry().getBestDiscountedPrice(orderDetails);
    }
}

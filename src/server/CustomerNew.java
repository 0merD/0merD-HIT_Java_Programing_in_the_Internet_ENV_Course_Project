package server;

import java.util.HashMap;
import java.util.Map;

public class CustomerNew extends CustomerAbstract{

    public CustomerNew(String fullName, String idNumber, String phoneNumber, CustomerTypeEnum customerType, double totalSpent) {
        super(fullName, idNumber, phoneNumber, customerType, totalSpent);

        CustomerDiscountsRegistry registry = getCustomerDiscountsRegistry();
        registry.addStrategy("No Discount", new DiscountStrategyNoDiscount());
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

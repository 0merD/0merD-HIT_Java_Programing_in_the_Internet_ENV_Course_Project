package server;

import java.util.HashMap;
import java.util.Map;

public class CustomerNew extends CustomerAbstract{

    public CustomerNew(String fullName, String idNumber, String phoneNumber, CustomerTypeEnum customerType, double totalSpent) {
        super(fullName, idNumber, phoneNumber, customerType, totalSpent);

        CustomerDiscountsRegistry registry = getCustomerDiscountsRegistry();
        registry.addStrategy("No Discount", new DiscountStrategyNoDiscount());
        HashMap<Integer, Double> newQuantityThresholds = new HashMap<>();
        newQuantityThresholds.put(2, 0.10);
        newQuantityThresholds.put(5, 0.15);
        newQuantityThresholds.put(10, 0.20);
        registry.addStrategy("Quantity", new DiscountStrategyQuantity(newQuantityThresholds));
    }

    @Override
    public double applyBestDiscount(OrderDetails orderDetails) {
        return getCustomerDiscountsRegistry().getBestDiscountedPrice(orderDetails);
    }
}

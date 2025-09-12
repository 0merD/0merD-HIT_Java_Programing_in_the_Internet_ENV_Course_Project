package server;

import server.enums.CustomerTypeEnum;

import java.util.HashMap;

public class CustomerNew extends CustomerAbstract{

    public CustomerNew(String fullName, String idNumber, String phoneNumber, CustomerTypeEnum customerType, double totalSpent) {
        super(fullName, idNumber, phoneNumber, customerType, totalSpent);

        CustomerDiscountsRegistry registry = getCustomerDiscountsRegistry();
        registry.addStrategy("No Discount", new DiscountStrategyNoDiscount());
        HashMap<Integer, Double> newQuantityDiscountStrategy = new HashMap<>();
        newQuantityDiscountStrategy.put(2, 0.10);
        newQuantityDiscountStrategy.put(5, 0.15);
        newQuantityDiscountStrategy.put(10, 0.20);
        registry.addStrategy("Quantity", new DiscountStrategyQuantity(newQuantityDiscountStrategy));
    }

    @Override
    public double applyBestDiscount(OrderDetails orderDetails) {
        return getCustomerDiscountsRegistry().getBestDiscountedPrice(orderDetails);
    }
}

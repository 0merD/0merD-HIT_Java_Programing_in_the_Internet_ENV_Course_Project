package server;

import java.util.Map;

public class DiscountStrategyPercentageCustomerType implements StrategyDiscount {

    private final Map<CustomerTypeEnum, Double> discountPercentagePerCustomerType;

    public DiscountStrategyPercentageCustomerType(Map<CustomerTypeEnum, Double> discountPercentagesMap) {
        //Todo: validate discount percentages are between a reasonable range else throw exception.
        this.discountPercentagePerCustomerType = discountPercentagesMap;
    }

    @Override
    public double applyDiscount(OrderDetails orderDetails) {
        double adjustedCost = orderDetails.getTotalPrice();
        CustomerTypeEnum customerType = orderDetails.getCustomer().getCustomerType();


        double discount = discountPercentagePerCustomerType.getOrDefault(customerType, 0.0);

        adjustedCost = adjustedCost * (1 - discount);

        return adjustedCost;
    }

    @Override
    public String getDescription() {
        return "Total price reduced by a percentage based on customer type, (VIP/Returning/New).";
    }
}

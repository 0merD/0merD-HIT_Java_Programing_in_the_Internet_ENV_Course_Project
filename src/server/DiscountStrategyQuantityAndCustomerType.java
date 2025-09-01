package server;

import java.util.Map;

public class DiscountStrategyQuantityAndCustomerType implements StrategyDiscount {


    private final Map<CustomerTypeEnum, Integer> quantityThresholdPerCustomer;

    private final Map<CustomerTypeEnum, Double> discountPercentagePerCustomer;

    public DiscountStrategyQuantityAndCustomerType(
            Map<CustomerTypeEnum, Integer> quantityThresholdPerCustomer,
            Map<CustomerTypeEnum, Double> discountPercentagePerCustomer)
    {
        this.quantityThresholdPerCustomer = quantityThresholdPerCustomer;
        this.discountPercentagePerCustomer = discountPercentagePerCustomer;
    }

    @Override
    public double applyDiscount(OrderDetails orderDetails) {
        double adjustedCost = orderDetails.getTotalPrice();
        CustomerTypeEnum customerType = orderDetails.getCustomer().getCustomerType();
        int quantity = orderDetails.getQuantity();

        // Check if the quantity meets the threshold for this customer type
        int threshold = quantityThresholdPerCustomer.getOrDefault(customerType, Integer.MAX_VALUE);

        if (quantity >= threshold) {
            double discount = discountPercentagePerCustomer.getOrDefault(customerType, 0.0);
            adjustedCost = adjustedCost * (1 - discount);
        }

        return adjustedCost;
    }

    @Override
    public String getDescription() {
        return "Discount calculated based on a combination of Quantity of items and customer type.";
    }
}

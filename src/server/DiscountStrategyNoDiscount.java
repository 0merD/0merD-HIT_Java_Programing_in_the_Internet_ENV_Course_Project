package server;

public class DiscountStrategyNoDiscount implements StrategyDiscount {

    @Override
    public double calculatePriceAfterDiscount(OrderDetails orderDetails) {
        return orderDetails.getTotalPrice(); // No discount is applied
    }

    /**
     * The fallback no discount strategy.
     * @return
     */
    @Override
    public String getDescription() {
        return "No Discount.";
    }
}

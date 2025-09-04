package server;

public class DiscountStrategyPercentage implements StrategyDiscount {

    private final double discountPercentage;

    public DiscountStrategyPercentage(double discountPercentage) {

        if (discountPercentage < 0.0 || discountPercentage > 1.0) {
            throw new IllegalArgumentException(
                    "Invalid discount for " + discountPercentage + ": must be between 0.0 and 1.0");
        }

        this.discountPercentage = discountPercentage;
    }


    @Override
    public double calculatePriceAfterDiscount(OrderDetails orderDetails) {
        double adjustedCost = orderDetails.getTotalPrice();

        adjustedCost = adjustedCost * (1 - discountPercentage);

        return adjustedCost;
    }

    @Override
    public String getDescription() {
        return "Total price reduced by a percentage based on customer type, (VIP/Returning/New).";
    }
}

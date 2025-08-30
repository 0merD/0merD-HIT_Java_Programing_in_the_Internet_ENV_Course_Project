package server;

public class DiscountStrategyBuyXGetOneFree implements StrategyDiscount {
    private final int itemsToBuyXGetOneFree;
    private final double unitPrice;

    public DiscountStrategyBuyXGetOneFree(int itemsToBuyXGetOneFree, double unitPrice) {
        this.itemsToBuyXGetOneFree = itemsToBuyXGetOneFree;
        this.unitPrice = unitPrice;
    }

    @Override
    public double applyDiscount(OrderDetails orderDetails) {
        int quantity = orderDetails.getQuantity();

        int freeItems = quantity / itemsToBuyXGetOneFree;

        return orderDetails.getTotalPrice() - (freeItems * unitPrice);
    }

    @Override
    public String getDescription() {
        return "Discount calculated based on Quantity of times, Buy X get One for free.";
    }
}

package server;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DiscountStrategyQuantity implements StrategyDiscount {

    // Map<QuantityThreshold, DiscountRate>
    private Map<Integer, Double> quantityThresholds;

    public DiscountStrategyQuantity(HashMap<Integer, Double> quantityThresholds) {
        this.quantityThresholds = quantityThresholds;
    }

    @Override
    public double calculatePriceAfterDiscount(OrderDetails orderDetails) {

        double originalPrice = orderDetails.getTotalPrice();
        int qty = orderDetails.getQuantity();
        double discountRate = 0.0;

        // Highest quantity still smaller or equal to qty
        for (Map.Entry<Integer, Double> entry : quantityThresholds.entrySet()) {
            int threshold = entry.getKey();
            double rate = entry.getValue();

            if (qty >= threshold && rate > discountRate) {
                discountRate = rate;
            }
        }

        return originalPrice * (1 - discountRate);
    }

    @Override
    public String getDescription() {
        return "Discount calculated based on a combination of Quantity of items and customer type.";
    }
}

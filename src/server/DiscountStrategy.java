package server;

public class DiscountStrategy {

    /**
     * Apply discount based on customer status and quantity purchased.
     * @param totalPrice original total price
     * @param quantity quantity of items bought
     * @param customerStatus NEW, RETURNING, VIP
     * @return discounted price
     */
    public static double applyDiscount(double totalPrice, int quantity, String customerStatus) {
        double discount = 0.0;

        switch (customerStatus.toUpperCase()) {
            case "NEW":
                discount = 0.0;
                break;
            case "RETURNING":
                discount = 0.05;
                if (quantity >= 10) discount += 0.05;
                break;
            case "VIP":
                discount = 0.15;
                if (quantity >= 10) discount += 0.05;
                break;
            default:
                throw new IllegalArgumentException("Unknown customer status: " + customerStatus);
        }

        return totalPrice * (1 - discount);
    }
}

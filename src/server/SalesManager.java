package server;

public class SalesManager {
    private static SalesManager instance;
    private final InventoryManager inventoryManager;
    private final CustomerManager customerManager;

    // Get the singleton object instances
    private SalesManager() {
        this.inventoryManager = InventoryManager.getInstance();
        this.customerManager = CustomerManager.getInstance();
    }

    public static synchronized SalesManager getInstance() {
        if (instance == null) {
            instance = new SalesManager();
        }
        return instance;
    }

    public SalesResult processSale(SalesRequest saleRequest) {
        SalesResult salesResult = new SalesResult();

        Integer branchNumber = saleRequest.getBranchNumber();
        String productId = saleRequest.getProductId();
        Integer  quantity = saleRequest.getQuantity();


        // 1. Validate product exists
        if (!ProductsCatalog.existsProductIdentifier(productId)) {
            salesResult.setSuccess(false);
            salesResult.setMessage(String.format("Product with product id: %s does not exist", productId));
            return salesResult;
        }

        // 2. Validate sufficient stock
        if (!inventoryManager.hasSufficientStock(branchNumber, productId, quantity)) {
            salesResult.setSuccess(false);
            salesResult.setMessage("Insufficient stock");
            return salesResult;
        }

        CustomerAbstract customer = saleRequest.getCustomer();
        double originalPrice = ProductsCatalog.getProductPrice(productId) * quantity;

        OrderDetails orderDetails = new OrderDetails(originalPrice, branchNumber, customer);
        double discountedPrice = customer.applyBestDiscount(orderDetails);

        salesResult.setOriginalPrice(originalPrice);
        salesResult.setDiscountApplied(originalPrice-discountedPrice);
        salesResult.setFinalPrice(discountedPrice);

        // 5. Reduce inventory
        try {
            inventoryManager.reduceStock(branchNumber, productId, quantity);
            salesResult.setSuccess(true);
            salesResult.setMessage("Sale processed successfully");
        } catch (Exception e) {
            salesResult.setSuccess(false);
            salesResult.setMessage("Error reducing stock: " + e.getMessage());
            e.printStackTrace();
        }

        return salesResult;
    }
}

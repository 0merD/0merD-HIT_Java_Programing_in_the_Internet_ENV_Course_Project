package server;

import java.util.List;

public class ProductsManager {

    private ProductsDataProvider dataProvider; // Current implementation uses json files, could be later replaced with DB

    //Todo: make sure relevant methods are added a lock
    public ProductsManager(ProductsDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }


    public Product getProductById(String productId) {
        return dataProvider.getProductById(productId);
    }

    public List<Product> getAllProducts(){
        return dataProvider.getAllProducts();
    }

    public void addProduct(Product product) {
        dataProvider.addProduct(product);
    }

    public boolean updateProduct(Product product) {
        return dataProvider.updateProduct(product);
    }

    public boolean deleteProduct(String productId) {
        return  dataProvider.deleteProduct(productId);
    }



    //Todo: Don't forget to delete this main (written for testing purposes).
    public static void main(String[] args) {

        ProductsDataProvider provider = new JsonProductsDataProvider();
        ProductsManager manager = new ProductsManager(provider);

        // 1. List all products
        System.out.println("=== All Products ===");
        List<Product> products = manager.getAllProducts();
        for (Product p : products) {
            System.out.println(p.getProductStringIdentifier() + " - " + p.getName() + ": " + p.getPrice() + "$");
        }

        System.out.println();

        // 2. Get a product by ID
        String searchId = "C003";
        Product hoodie = manager.getProductById(searchId);
        if (hoodie != null) {
            System.out.println("Found product " + searchId + ": " + hoodie.getName() + " - " + hoodie.getPrice() + "$");
        } else {
            System.out.println("Product " + searchId + " not found.");
        }

        System.out.println();

        // 3. Add a new product
        Product scarf = new Product("C007", "Scarf", "Warm wool scarf", 18.0);
        manager.addProduct(scarf);
        System.out.println("Added product: " + scarf.getName());

        // 4. Verify it's added
        Product check = manager.getProductById("C007");
        System.out.println("Check added product: " + (check != null ? check.getName() : "Not found"));

        System.out.println();

        // 5. Update a product
        scarf.setPrice(20.0);
        manager.updateProduct(scarf);
        System.out.println("Updated scarf price to 20.0$");

        // 6. Delete a product
        manager.deleteProduct("C002"); // delete Jeans
        System.out.println("Deleted product C002");

        // 7. List products again
        System.out.println("\n=== Products After Changes ===");
        for (Product p : manager.getAllProducts()) {
            System.out.println(p.getProductStringIdentifier() + " - " + p.getName() + ": " + p.getPrice() + "$");
        }
    }
}

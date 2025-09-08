package server;

import java.util.List;

public interface ProductsDataProvider {
    /**
     * This interface Defines the contract for getting and setting products data.
     * Current implementation will use Json files, but in the future could be replaced with a real database
     * without breaking existing code.
     */

        Product getProductById(String productId);

        List<Product> getAllProducts();

        void addProduct(Product product);

        boolean updateProduct(Product product);

        boolean deleteProduct(String productId);

}

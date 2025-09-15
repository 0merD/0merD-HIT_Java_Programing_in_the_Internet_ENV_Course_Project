package server.managers;

import server.Product;
import server.ProductsDataProvider;

import java.util.List;

public class ProductsManager {

    // Current implementation uses json files, could be later replaced with real DB (sql/mongo etc).
    private ProductsDataProvider dataProvider;


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

}

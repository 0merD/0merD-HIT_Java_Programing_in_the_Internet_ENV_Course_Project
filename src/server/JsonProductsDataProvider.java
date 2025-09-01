package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;



public class JsonProductsDataProvider implements ProductsDataProvider {

    private final Path filePath = Paths.get("resources","products.json");

    private final GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();

    private List<Product> products;

    public JsonProductsDataProvider() {

        loadProducts();
    }

    private void loadProducts() {
        try (FileReader reader = new FileReader(filePath.toString())) {
            Type listType = new TypeToken<ArrayList<Product>>(){}.getType();
            List<Product> products = gsonBuilder.create().fromJson(reader, listType);

            if (products != null) {
                this.products = products;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveProducts() {
        try (FileWriter writer = new FileWriter(filePath.toString())) {
            gsonBuilder.create().toJson(products, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save products: " + e.getMessage(), e);
        }
    }

    @Override
    public Product getProductById(String productId) {
        Product product = null;

        for (Product p : products) {
            if (p.getProductStringIdentifier().equals(productId)) {
                product = p;
                break;
            }
        }

        return product;
    }

    @Override
    public List<Product> getAllProducts() {
        return new ArrayList<>(products);
    }

    @Override
    public void addProduct(Product product) {
        products.add(product);
        saveProducts();
    }

    @Override
    public boolean updateProduct(Product product) {
        boolean isSuccesfullUpdate = false;

        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getProductStringIdentifier().equals(product.getProductStringIdentifier())) {
                products.set(i, product);
                saveProducts();
                isSuccesfullUpdate =  true;
            }
        }

        return isSuccesfullUpdate;
    }


    @Override
    public boolean deleteProduct(String productId) {
        boolean removed = products.removeIf(p -> p.getProductStringIdentifier().equals(productId));
        if (removed) {
            saveProducts();
        }
        return removed;
    }
}

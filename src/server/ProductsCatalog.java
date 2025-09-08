package server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductsCatalog {

    private static final Map<String, Product> PRODUCTS = new HashMap<>();
    private static final Path DEFAULT_JSON_PATH = Paths.get("resources", "products.json");
    private static volatile boolean loaded = false;

    private ProductsCatalog() {
        // prevent instantiation
    }

    // Lazy Loading
    private static void ensureLoaded() {
        if (!loaded) {

            synchronized (ProductsCatalog.class) {

                if (!loaded) { // double-check inside lock
                    try {
                        loadProductsFromJson(DEFAULT_JSON_PATH.toString());
                        loaded = true;
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to load products from JSON", e);
                    }
                }
            }
        }
    }

    public static void loadProductsFromJson(String filePath) throws IOException {

        Gson gson = new Gson();
        Type productListType = new TypeToken<List<Product>>() {}.getType();

        try (FileReader reader = new FileReader(filePath)) {
            List<Product> productList = gson.fromJson(reader, productListType);
            synchronized (ProductsCatalog.class) { // lock to avoid concurrent modifications
                PRODUCTS.clear();
                for (Product product : productList) {
                    PRODUCTS.put(product.getProductIdentifier(), product);
                }
            }
        }
    }

    public static boolean existsProductIdentifier(String productIdentifier) {
        ensureLoaded();
        return PRODUCTS.containsKey(productIdentifier);
    }

    public static Product getProduct(String productIdentifier) {
        ensureLoaded();
        return PRODUCTS.get(productIdentifier);
    }

    public static Collection<Product> getAllProducts() {
        ensureLoaded();
        return PRODUCTS.values();
    }

    public static double getProductPrice(String productId) {
        return  getProduct(productId).getPrice();
    }
}
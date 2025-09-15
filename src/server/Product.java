package server;

public class Product {

    private String name;
    private String description;
    private double price;
    private String productIdentifier; // "Makat" // Modified to String

    public Product() {}

    public Product(String catalogStringIdentifier, String name, String description, double price) {
        this.productIdentifier = catalogStringIdentifier;
        this.name = name;
        this.description = description;
        this.price = price;
    }


    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getProductIdentifier() {
        return productIdentifier;
    }


}

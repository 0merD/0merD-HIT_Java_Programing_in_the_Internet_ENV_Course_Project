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

    //Todo: verify with Roy this is ok.
//    public void setProductStringIdentifier(String productStringIdentifier) {
//        this.productStringIdentifier = productStringIdentifier;
//    }
//
//    public void setPrice(double price) {
//        this.price = price;
//    }
//
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
}

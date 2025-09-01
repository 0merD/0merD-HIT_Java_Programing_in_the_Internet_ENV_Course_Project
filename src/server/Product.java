package server;

public  class Product {
    String name;
    String description;
    double price;
    String productStringIdentifier; // "Makat" // Modified to String


    public Product() {}

    public Product(String catalogStringIdentifier, String name, String description, double price) {
        this.productStringIdentifier = catalogStringIdentifier;
        this.name = name;
        this.description = description;
        this.price = price;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getProductStringIdentifier() {
        return productStringIdentifier;
    }

    public void setProductStringIdentifier(String productStringIdentifier) {
        this.productStringIdentifier = productStringIdentifier;
    }
}

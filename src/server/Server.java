package server;

public class Server {

    ClientInstanceHandler clientInstanceHandler;
    CustomerManager customerManager;
    InventoryManager inventoryManager;

    public Server() {
        clientInstanceHandler = new ClientInstanceHandler();
        customerManager = new CustomerManager();
        inventoryManager = new InventoryManager();
    }
}

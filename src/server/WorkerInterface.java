package server;

import shared.*;

public interface WorkerInterface {

    void findCustomer(int id);

    Purchase openPurchase();

    Boolean handlePurchase(Purchase purchase); //Maybe not boolean?

    Boolean addCustomer(CustomerInterface customerInterface);

}

package server;

import java.util.List;

public interface CustomerStorage {

    void saveCustomers(List<CustomerAbstract> customersToSave);

    List<CustomerAbstract> loadCustomersFromStorage();
}

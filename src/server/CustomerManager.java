package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerManager implements CustomerStorage {

    private static CustomerManager instance;

    private static int nextCustomerId = 1;

    private final Path FILE_PATH = Paths.get("resources", "customers.json");

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Uses custId (String) as the unique id per customer
    private final Map<String, CustomerAbstract> customers = new HashMap<>();

    private CustomerManager() {}

    public static synchronized CustomerManager getInstance() {
        if (instance == null) {
            instance = new CustomerManager();
            instance.loadCustomersFromStorage();
        }
        return instance;
    }

    @Override
    public void saveCustomers(List<CustomerAbstract> customersToSave) {
        try (FileWriter writer = new FileWriter(FILE_PATH.toString())) {
            gson.toJson(customersToSave, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public List<CustomerAbstract> loadCustomersFromStorage() {

        List<CustomerAbstract> loadedList = new ArrayList<>();

        try (
                FileReader reader = new FileReader(FILE_PATH.toString())
        ) {
            Type listType = new TypeToken<List<CustomerJson>>(){}.getType();
            List<CustomerJson> customersFromJson = new Gson().fromJson(reader, listType);

            if (customersFromJson != null) {
                for (CustomerJson json : customersFromJson) {
                    CustomerAbstract customer = json.toCustomer(); // uses factory
                    customers.put(customer.getCustId(), customer);
                    loadedList.add(customer);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return loadedList;
    }

    public void addCustomer(CustomerAbstract customer) {

        if (!customers.containsKey(customer.getCustId())) {
            customers.put(customer.getCustId(), customer);
            saveCustomers(getAllCustomers());
        } else {
            System.out.println("Customer with ID " + customer.getCustId() + " already exists. Skipping.");
        }
    }


    public List<CustomerAbstract> getAllCustomers() {
        return new ArrayList<>(customers.values());
    }


    // Helper class for JSON mapping
    private static class CustomerJson {
        public String fullName;
        public String custId;
        public String phoneNumber;
        public CustomerTypeEnum customerType;

        public CustomerJson(CustomerAbstract customer) {
            this.fullName = customer.getFullName();
            this.custId = customer.getCustId();
            this.phoneNumber = customer.getPhoneNumber();
            this.customerType = customer.getCustomerType();
        }

        // Used to convert back to customer using the CustomerFactory
        public CustomerAbstract toCustomer() {
            return CustomerFactory.createCustomer(
                    fullName,
                    custId,
                    phoneNumber,
                    customerType
            );
        }
    }

    // TODO: Delete this main.
    // main for testing only
    public static void main(String[] args) {

        // Create the manager and load existing customers
        CustomerManager manager = new CustomerManager();
        List<CustomerAbstract> loadedCustomers = manager.loadCustomersFromStorage();

        System.out.println("Loaded customers from JSON:");
        loadedCustomers.forEach(System.out::println);

        // Add new customers
        CustomerNew alice = new CustomerNew("Alice Cohen", "123456789", "050-1111111", CustomerTypeEnum.New);
        CustomerVip charlie = new CustomerVip("Charlie Gold", "555555555", "053-3333333", CustomerTypeEnum.Vip);
        CustomerReturning bob = new CustomerReturning("Bob Levi", "987654321", "052-2222222", CustomerTypeEnum.Returning);

        manager.addCustomer(alice);
        manager.addCustomer(bob);
        manager.addCustomer(charlie);

        System.out.println("\nAll customers after adding:\n");
        manager.getAllCustomers().forEach(System.out::println);

        // Save to JSON
        manager.saveCustomers(manager.getAllCustomers());

        System.out.println("\nCustomers saved to JSON successfully!");
    }


}
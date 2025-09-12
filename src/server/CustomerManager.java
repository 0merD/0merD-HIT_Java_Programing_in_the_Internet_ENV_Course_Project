package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import server.enums.CustomerTypeEnum;

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
    private transient final Map<String, CustomerAbstract> customers = new HashMap<>();

    private CustomerManager() {}

    public static synchronized CustomerManager getInstance() {
        if (instance == null) {
            instance = new CustomerManager();
            instance.loadCustomersFromStorage();
        }
        return instance;
    }

    public static void promoteCustomer(CustomerAbstract customer) {
        CustomerTypeEnum currentType = customer.getCustomerType();
        double totalSpent = customer.getTotalSpent();
        boolean promoted = false;

        switch (currentType) {
            case New:
                if (totalSpent >= ThresholdConsts.RETURNING_CUSTOMER_THRESHOLD) {
                    customer.setCustomerType(CustomerTypeEnum.Returning);
                    promoted = true;
                }
                break;

            case Returning:
                if (totalSpent >= ThresholdConsts.VIP_CUSTOMER_THRESHOLD) {
                    customer.setCustomerType(CustomerTypeEnum.Vip);
                    promoted = true;
                }
                break;

            default:
                break;
        }

        if (promoted) {
            CustomerManager.getInstance().saveCustomers(CustomerManager.instance.getAllCustomers());
        }

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
        public double totalSpent;

        public CustomerJson(CustomerAbstract customer) {
            this.fullName = customer.getFullName();
            this.custId = customer.getCustId();
            this.phoneNumber = customer.getPhoneNumber();
            this.customerType = customer.getCustomerType();
            this.totalSpent = customer.getTotalSpent();
        }

        // Used to convert back to customer using the CustomerFactory
        public CustomerAbstract toCustomer() {
            return CustomerFactory.createCustomer(
                    fullName,
                    custId,
                    phoneNumber,
                    customerType,
                    totalSpent
            );
        }
    }

    //Todo: delete this main
//    public static void main(String[] args) {
//        CustomerManager manager = CustomerManager.getInstance();
//
//        // Load customer C001
//
//        List<CustomerAbstract> lst =manager.getAllCustomers();
//        CustomerAbstract noam = lst.stream()
//                .filter(c -> c.getCustId().equals("C001")) // condition
//                .findFirst()                               // returns Optional<CustomerAbstract>
//                .orElse(null);
//
//        // Register a purchase that should trigger promotion
//        double purchaseAmount = 160.0;  // totalSpent goes from 50 -> 210
//        noam.addSpent(purchaseAmount);
//        CustomerManager.promoteCustomer(noam);
//
//        System.out.println("After purchase: " + noam);
//
//        // Save changes to JSON
//        manager.saveCustomers(manager.getAllCustomers());
//    }

}
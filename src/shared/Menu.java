package shared;

import java.util.LinkedHashMap;
import java.util.Scanner;

public class Menu {
    private final LinkedHashMap<String, MenuAction> menuOptions = new LinkedHashMap<>();
    private final String menuTitle;

    public Menu(String menuTitle) {
        this.menuTitle = menuTitle;
    }


    public void addOption(String optionLabel, MenuAction action) {
        menuOptions.put(optionLabel, action);
    }

    public void displayAndRun() {
        Scanner scanner = new Scanner(System.in);
        int i = 1;
        int userChoice;

        while (true) {
            System.out.println("\n" + menuTitle);
            for (String label : menuOptions.keySet()) {
                System.out.println(i + ". " + label);
                i++;
            }
            System.out.print("Select option (or 0 to exit): ");


            try {
                userChoice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            if (userChoice == 0) {
                break; // exit menu
            }
            if (userChoice < 0 || userChoice > menuOptions.size()) {
                System.out.println("Invalid userChoice, try again.");
                continue;
            }

            // Get selected action and execute
            MenuAction action = (MenuAction) menuOptions.values().toArray()[userChoice - 1];
            action.execute();
        }
    }
}


//import static org.junit.jupiter.api.Assertions.*;
//import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import shared.Menu;

public class MenuTest {

    // Helper to redirect input/output and run menu with given input
    private String runMenuWithInput(String input, Menu menu) {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        try {
            ByteArrayInputStream testIn = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
            System.setIn(testIn);

            ByteArrayOutputStream testOut = new ByteArrayOutputStream();
            System.setOut(new PrintStream(testOut));

            menu.displayAndRun();

            return testOut.toString(StandardCharsets.UTF_8);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    @Test
    public void testValidOptionRunsAction() {
        Menu menu = new Menu("Test Menu");

        final boolean[] actionRun = {false};
        menu.addOption("Say Hello", () -> actionRun[0] = true);

        // Input: choose option 1, then exit with 0
        String input = "1\n0\n";

        String output = runMenuWithInput(input, menu);

        assertTrue(actionRun[0], "The menu action should have run.");
        assertTrue(output.contains("Test Menu"), "Menu title should appear.");
        assertTrue(output.contains("Say Hello"), "Menu option should appear.");
    }

    @Test
    public void testZeroExitsMenuImmediately() {
        Menu menu = new Menu("Test Menu");

        final boolean[] actionRun = {false};
        menu.addOption("Option 1", () -> actionRun[0] = true);

        // Input: immediately exit with 0
        String input = "0\n";

        String output = runMenuWithInput(input, menu);

        assertFalse(actionRun[0], "No action should run if user exits immediately.");
        assertTrue(output.contains("Test Menu"), "Menu title should appear before exit.");
    }

    @Test
    public void testOutOfRangeInputShowsError() {
        Menu menu = new Menu("Test Menu");

        final boolean[] actionRun = {false};
        menu.addOption("Option 1", () -> actionRun[0] = true);

        // Input: invalid option 5, then exit 0
        String input = "5\n0\n";

        String output = runMenuWithInput(input, menu);

        assertFalse(actionRun[0], "No action should run for out-of-range input.");
        assertTrue(output.contains("Invalid userChoice"), "Error message for invalid input should appear.");
    }
}

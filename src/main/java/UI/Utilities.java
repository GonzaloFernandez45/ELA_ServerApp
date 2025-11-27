package UI;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pojos.*;

public class Utilities {
    public static int readInteger(String question) {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader buffer = new BufferedReader(input);
        int num;
        String line;
        while (true) {
            try {
                System.out.print(question);
                line = buffer.readLine();
                num = Integer.parseInt(line);
                return num;

            } catch (IOException ioe) {
                System.out.println(" ERROR: Unable to read.");

            } catch (NumberFormatException nfe) {
                System.out.println(" ERROR: Must be a whole number.");
            }
        }
    }
    /**
     * Reads a date from the user, asking for day, month, and year separately.
     * Ensures the date is valid and repeats the prompt if the input is not a valid date.
     *
     * @param question The prompt to display to the user.
     * @return A valid LocalDate input by the user.
     */
    public static LocalDate readDate(String question)  {

        while (true) {
            try {
                System.out.println(question);
                int day = readInteger("   Day: ");
                int month = readInteger("   Month: ");
                int year = readInteger("   Year: ");
                return LocalDate.of(year, month, day);

            } catch (DateTimeException e) {
                System.out.println(" ERROR: Introduce a valid date.");
            }

        }
    }
    /**
     * Reads a string from the user after displaying a prompt.
     * Ensures that the input is valid and repeats the prompt if an error occurs during reading.
     *
     * @param question The prompt to display to the user.
     * @return A valid string input by the user.
     */
    public static String readString(String question) {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader buffer = new BufferedReader(input);
        String line;
        while (true) {
            try {
                System.out.print(question);
                line = buffer.readLine();
                return line;

            } catch (IOException ioe) {
                System.out.println(" ERROR: Unable to read.");
            }
        }
    }
    /**
     * Validates an email string to ensure it follows a proper email format.
     *
     * @param email The email string to validate.
     * @return {@code true} if the email format is valid; {@code false} otherwise.
     */
    public static boolean checkEmail(String email){
        Pattern pattern = Pattern.compile("([a-z0-9]+(\\.?[a-z0-9])*)+@(([a-z]+)\\.([a-z]+))+");
        Matcher mather = pattern.matcher(email);
        if (mather.find()) {
            return true;
        } else {
            System.out.println("Please follow the email format: example@example.com");
            return false;
        }
    }
    /**
     * Formats a given MAC address string by adding colons (':') after every two characters.
     *
     * @param input The unformatted MAC address string.
     * @return The formatted MAC address string.
     */
    public static String formatMacAdress(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            result.append(input.charAt(i));
            if ((i + 1) % 2 == 0 && i < input.length() - 1) {
                result.append(":");
            }
        }
        return result.toString();
    }

    /**
     * Placeholder method for registering patient data.
     * Currently, this method returns {@code null}.
     *
     * @return A {@code Patient} object containing registered data or {@code null}.
     */
    public static Doctor registerDoctortData()
    {
        Doctor doctor = null;


        return doctor;
    }

}

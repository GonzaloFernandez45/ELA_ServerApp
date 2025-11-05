package pojos;

import java.util.ArrayList;
import java.util.List;

public class Signal {

    private List<Integer> values;

    public enum SignalType {
        EMG,
        ACC
    }
    private SignalType signalType;


    /*
     * Using ArrayList for signal samples:
     * - Fast indexed access and iteration (O(1) get), good cache locality.
     * - Amortized O(1) appends when collecting samples from the stream.
     * - Lower memory overhead than LinkedList (no per-node objects).
     * - We don't remove from the front frequently; if we did, a deque/ring buffer would be better.
     * This fits the typical BITalino pattern: accumulate samples, then process/serialize sequentially.
     */

    public Signal(SignalType signalType) {
        this.values = new ArrayList<>();
        this.signalType = signalType;
    }

    public List<Integer> getValues() {
        return values;
    }

    public void setValues(List<Integer> values) {
        this.values = values;
    }

    public SignalType getSignalType() {
        return signalType;
    }

    public void setSignalType(SignalType signalType) {
        this.signalType = signalType;
    }

    @Override
    public String toString() {
        return "Signal{" +
                "values=" + values +
                ", signalType=" + signalType +
                '}';
    }

    /* HACER METODOS DE VALUES-TOSTRING Y STRING-TOVALUES */

    /*
     * Converts the list of integers 'values' into a String with space-separated values.
     * It iterates through the list, appends each number to the StringBuilder,
     * and adds the separator only between elements (no trailing space).
     * Returns an empty string if the list is empty.
     * Time complexity: O(n).
     */

    public String valuesToString() {
        StringBuilder message = new StringBuilder();
        String separator = " ";

        for (int i = 0; i < values.size(); i++) {
            message.append(values.get(i));
            if (i < values.size() - 1) {
                message.append(separator);
            }
        }

        return message.toString();
    }

    public List<Integer> stringToValues(String str) {
        values.clear(); // Clear the existing values before adding new ones.
        String[] samples = str.split(" "); // Split the string by spaces.

        for (String sample : samples) {
            try {
                values.add(Integer.parseInt(sample)); // Convert each token to an integer and add it to the list.
            } catch (NumberFormatException e) {
                // Handle errors if a token is not a valid integer.
                System.out.println("Error converting value: " + sample);
            }
        }

        return values;
    }
}

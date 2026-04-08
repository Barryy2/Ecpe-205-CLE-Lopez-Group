import java.util.List;

public class CalculationsLogic {
    /**
     * Compute Total Assets
     * Auto update asset when value changes
     * Validation of calculations
     */

    private double subBal;
    private double totalBal;

    // Constructor
    public CalculationsLogic(double subBal, double totalBal) {
        this.subBal = subBal;
        this.totalBal = totalBal;
    }

    // Empty constructor to start at 0
    public CalculationsLogic() {
        this.subBal = 0.0;
        this.totalBal = 0.0;
    }

    // Getters
    public double getSubBal() {
        return subBal;
    }

    public double getTotalBal() {
        return totalBal;
    }

    // Setters
    public void setSubBal(double subBal) {
        this.subBal = subBal;
    }

    public void setTotalBal(double totalBal) {
        this.totalBal = totalBal;
    }

    // --- NEW COMPUTATION METHOD ---
    /**
     * Takes a list of strings from the UI, validates them, and computes the total.
     */
    public double computeTotalAssets(List<String> amounts) {
        double total = 0.0;
        for (String text : amounts) {
            // Strip away letters/symbols, keep only numbers and decimals
            String cleanText = text.replaceAll("[^\\d.]", "").trim();
            if (!cleanText.isEmpty()) {
                try {
                    total += Double.parseDouble(cleanText);
                } catch (NumberFormatException ignored) {
                    // Ignore if they typed multiple decimals like "12..50"
                }
            }
        }

        // Update the class variable and return the result
        this.totalBal = total;
        return this.totalBal;
    }
}
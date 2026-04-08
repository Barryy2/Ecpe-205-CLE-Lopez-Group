import javax.swing.*;
import java.util.List;

public class CalculationsLogic {


    private double subBal;      // Used for sub accounts
    private double totalBal;    // Used for total net


    public CalculationsLogic(double subBal, double totalBal) {
        this.subBal = subBal;
        this.totalBal = totalBal;
    }


    public CalculationsLogic() {
        this.subBal = 0.0;
        this.totalBal = 0.0;
    }


    public double getSubBal() {
        return subBal;
    }

    public double getTotalBal() {
        return totalBal;
    }


    public void setSubBal(double subBal) {
        this.subBal = subBal;
    }

    public void setTotalBal(double totalBal) {
        this.totalBal = totalBal;
    }


    public double computeTotalAssets(List<String> amounts) {
        double total = 0.0;
        for (String text : amounts) {

            String cleanText = text.replaceAll("[^\\d.]", "#").trim();

            if (cleanText.contains("#")){
                JOptionPane.showMessageDialog(null, "Contains non-numeric values, please try again.");
                return totalBal;
            }

            int dec = 0;
            for (char c : cleanText.toCharArray()){
                if (c == '.'){
                    dec++;
                }

                if (dec > 1){
                    JOptionPane.showMessageDialog(null, "Amount entered contains too many decimals, please try again.");
                    return totalBal;
                }
            }

            if (!cleanText.isEmpty()) {
                try {
                    total += Double.parseDouble(cleanText);
                } catch (NumberFormatException ignored) {

                }
            }
        }


        this.totalBal = total;
        return this.totalBal;
    }
}
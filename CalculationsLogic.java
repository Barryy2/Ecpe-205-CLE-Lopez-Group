public class CalculationsLogic {
    /**
    Compute Total Assets
    Auto update asset when value changes
    Validation of calculations
     */

    private int subBal;
    private int totalBal;

    // Constructor
    public CalculationsLogic(int subBal, int totalBal) {
        this.subBal = subBal;
        this.totalBal = totalBal;
    }

    // Getters
    public int getSubBal() {
        return subBal;
    }
    public int getTotalBal() {
        return totalBal;
    }

    // Setters
    public void setSubBal(int subBal) {
        this.subBal = subBal;
    }
    public void setTotalBal(int totalBal) {
        this.totalBal = totalBal;
    }

    // Logics
    public void totalNet(int subBal){
        this.totalBal += subBal;
    }
}

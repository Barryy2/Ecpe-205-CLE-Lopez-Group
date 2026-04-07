import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // SwingUtilities ensures the UI is built on the correct thread
        SwingUtilities.invokeLater(() -> {
            TrackerUI appWindow = new TrackerUI();
            appWindow.setVisible(true);
        });
    }
}
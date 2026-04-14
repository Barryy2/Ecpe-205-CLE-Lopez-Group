import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
//<<<<<<< HEAD
        System.out.println("Hello owrld");
//=======?
        // SwingUtilities ensures the UI is built on the correct thread
        SwingUtilities.invokeLater(() -> {
            TrackerUI appWindow = new TrackerUI();
            appWindow.setVisible(true);
        });
//>>>>>>> Inocencio_branch
    }
}
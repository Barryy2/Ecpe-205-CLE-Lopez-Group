    import javax.swing.SwingUtilities;

    public class Main {
        public static void main(String[] args) {
            System.out.println("Hello owrld");


            SwingUtilities.invokeLater(() -> {
                TrackerUI appWindow = new TrackerUI();
                appWindow.setVisible(true);
            });

        }
    }

    //
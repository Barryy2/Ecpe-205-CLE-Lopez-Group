import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class TrackerUI extends JFrame {

    private final JPanel listContainer;
    private final List<BankRow> bankRows = new ArrayList<>();
    private JLabel totalLabel;
    private BankRow selectedRow = null;

    private final DatabaseManager dbManager;
    private final CalculationsLogic calcLogic;

    private final int ROW_HEIGHT = 50;
    private final int COL2_WIDTH = 150;
    private final int COL3_WIDTH = 120;

    private final Color COLOR_BG = Color.WHITE;
    private final Color COLOR_HOVER = new Color(245, 248, 250);
    private final Color COLOR_SELECTED = new Color(230, 238, 245);
    private final Color COLOR_TEXT_MAIN = new Color(33, 37, 41);
    private final Color COLOR_GRIDLINE = new Color(210, 210, 210);
    private final Font FONT_MAIN = new Font("SansSerif", Font.PLAIN, 15);
    private final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 15);

    public TrackerUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        dbManager = new DatabaseManager();
        calcLogic = new CalculationsLogic();

        setTitle("Bank Accounts Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BG);

        // 1. Initialize List Container
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(COLOR_BG);

        // 2. RESTORE THE TAB LOOK (The "Dashboard" tab header)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 13));

        // This panel holds your original table content
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(COLOR_BG);
        tablePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_GRIDLINE));
        scrollPane.getViewport().setBackground(COLOR_BG);

        JPanel headerRow = new JPanel();
        headerRow.setLayout(new BoxLayout(headerRow, BoxLayout.X_AXIS));
        headerRow.add(createHeaderCell("Bank Details", 0, true));
        headerRow.add(createHeaderCell("Amount", COL2_WIDTH, false));
        headerRow.add(createHeaderCell("Actions", COL3_WIDTH, false));
        scrollPane.setColumnHeaderView(headerRow);

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // 3. RESTORE BOTTOM SPACING (Padding and Layout)
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(COLOR_BG);
        bottomPanel.setBorder(new EmptyBorder(15, 0, 0, 0)); // Bottom area padding

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.setBackground(COLOR_BG);
        JButton addBtn = createFlatButton("+ Add Bank", new Color(240, 240, 240), COLOR_TEXT_MAIN);
        addBtn.addActionListener(e -> addBankRow("New Bank", "", "", true));

        JButton removeBtn = createFlatButton("- Remove Selected", new Color(255, 240, 240), new Color(200, 50, 50));
        removeBtn.addActionListener(e -> {
            if (selectedRow != null) removeBankRow(selectedRow);
        });

        controls.add(addBtn);
        controls.add(removeBtn);

        JPanel totalsPanel = new JPanel(new BorderLayout());
        totalsPanel.setBackground(COLOR_BG);
        totalsPanel.setBorder(new EmptyBorder(15, 5, 0, 5));
        totalLabel = new JLabel("Total Net: 0.00");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        JButton saveBtn = createFlatButton("Save Data", new Color(40, 167, 69), Color.WHITE);
        saveBtn.addActionListener(e -> saveUIStateToDatabase());

        totalsPanel.add(totalLabel, BorderLayout.WEST);
        totalsPanel.add(saveBtn, BorderLayout.EAST);

        bottomPanel.add(controls);
        bottomPanel.add(totalsPanel);

        tablePanel.add(bottomPanel, BorderLayout.SOUTH);

        // Add the Dashboard panel into the TabbedPane to get that top "Dashboard" label back
        tabbedPane.addTab("Dashboard", tablePanel);
        add(tabbedPane, BorderLayout.CENTER);

        loadDataIntoUI();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { saveUIStateToDatabase(); }
        });

        setSize(850, 650);
        setLocationRelativeTo(null);
    }

    private void showEditPopup(BankRow row) {
        JDialog dialog = new JDialog(this, "Edit Profile", true);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(COLOR_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);

        JLabel preview = new JLabel("", SwingConstants.CENTER);
        preview.setPreferredSize(new Dimension(100, 100));
        preview.setBorder(BorderFactory.createLineBorder(COLOR_GRIDLINE));
        ImageIcon icon = scaleIcon(row.imagePath, 100, 100);
        if (icon != null) preview.setIcon(icon);
        else preview.setText("No Image");

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel title = new JLabel("Edit " + row.nameField.getText());
        title.setFont(FONT_BOLD);
        dialog.add(title, gbc);

        gbc.gridy = 1;
        dialog.add(preview, gbc);

        JButton upload = createFlatButton("Change Image", new Color(0, 123, 255), Color.WHITE);
        upload.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                String path = fc.getSelectedFile().getAbsolutePath();
                row.updateRowImage(path);
                preview.setIcon(scaleIcon(path, 100, 100));
                preview.setText("");
            }
        });
        gbc.gridy = 2; gbc.gridwidth = 1;
        dialog.add(upload, gbc);

        JButton close = createFlatButton("Done", Color.GRAY, Color.WHITE);
        close.addActionListener(e -> dialog.dispose());
        gbc.gridx = 1;
        dialog.add(close, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private ImageIcon scaleIcon(String path, int w, int h) {
        if (path == null || path.isEmpty()) return null;
        try {
            return new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Exception e) { return null; }
    }

    private JPanel createHeaderCell(String title, int width, boolean stretch) {
        JPanel p = new JPanel(new BorderLayout());
        if (stretch) p.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));
        else { Dimension d = new Dimension(width, ROW_HEIGHT); p.setPreferredSize(d); p.setMaximumSize(d); }
        p.setBackground(new Color(238, 242, 246));
        p.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.WHITE, new Color(180, 180, 180)));
        JLabel l = new JLabel(title, SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        p.add(l, BorderLayout.CENTER);
        return p;
    }

    private JButton createFlatButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBackground(bg); b.setForeground(fg);
        b.setOpaque(true); b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void addBankRow(String name, String amount, String path, boolean edit) {
        BankRow row = new BankRow(name, amount, path);
        bankRows.add(row);
        listContainer.add(row);
        calculateTotal();
        listContainer.revalidate();
        listContainer.repaint();
    }

    private void removeBankRow(BankRow row) {
        bankRows.remove(row);
        listContainer.remove(row);
        if (selectedRow == row) selectedRow = null;
        calculateTotal();
        listContainer.revalidate();
        listContainer.repaint();
    }

    private void selectRow(BankRow row) {
        if (selectedRow != null) selectedRow.setBackground(COLOR_BG);
        selectedRow = row;
        if (selectedRow != null) selectedRow.setBackground(COLOR_SELECTED);
    }

    private void calculateTotal() {
        List<String> amounts = new ArrayList<>();
        for (BankRow r : bankRows) amounts.add(r.amountField.getText());
        totalLabel.setText(String.format("Total Net: %,.2f", calcLogic.computeTotalAssets(amounts)));
    }

    private void loadDataIntoUI() {
        List<String[]> data = dbManager.loadAccounts();
        if (data.isEmpty()) {
            addBankRow("Bank 1", "", "", true);
        } else {
            for (String[] d : data) addBankRow(d[0], d[1], d.length > 2 ? d[2] : "", false);
        }
    }

    private void saveUIStateToDatabase() {
        List<String[]> toSave = new ArrayList<>();
        for (BankRow r : bankRows) toSave.add(new String[]{r.nameField.getText(), r.amountField.getText(), r.imagePath});
        try { dbManager.saveAccounts(toSave); } catch (Exception ignored) {}
    }

    private class BankRow extends JPanel {
        JTextField nameField, amountField;
        JLabel imgPlaceholder;
        String imagePath;

        public BankRow(String name, String amount, String path) {
            this.imagePath = path;
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setBackground(COLOR_BG);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));

            JPanel col1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
            col1.setOpaque(false);
            col1.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));
            col1.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, COLOR_GRIDLINE));

            imgPlaceholder = new JLabel("", SwingConstants.CENTER);
            imgPlaceholder.setPreferredSize(new Dimension(32, 32));
            imgPlaceholder.setOpaque(true);
            updateRowImage(path);

            nameField = new JTextField(name);
            nameField.setPreferredSize(new Dimension(200, 30));
            nameField.setBorder(null); nameField.setOpaque(false);

            col1.add(imgPlaceholder); col1.add(nameField);

            JPanel col2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
            col2.setOpaque(false);
            Dimension d2 = new Dimension(COL2_WIDTH, ROW_HEIGHT);
            col2.setPreferredSize(d2); col2.setMaximumSize(d2);
            col2.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, COLOR_GRIDLINE));

            amountField = new JTextField(amount);
            amountField.setPreferredSize(new Dimension(110, 30));
            amountField.setHorizontalAlignment(JTextField.RIGHT);
            amountField.setBorder(null); amountField.setOpaque(false);
            amountField.setFont(FONT_BOLD);
            amountField.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { calculateTotal(); }
                public void removeUpdate(DocumentEvent e) { calculateTotal(); }
                public void changedUpdate(DocumentEvent e) { calculateTotal(); }
            });
            col2.add(amountField);

            JPanel col3 = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 8));
            col3.setOpaque(false);
            Dimension d3 = new Dimension(COL3_WIDTH, ROW_HEIGHT);
            col3.setPreferredSize(d3); col3.setMaximumSize(d3);
            col3.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_GRIDLINE));

            JButton profileBtn = createFlatButton("Edit Profile", new Color(233, 236, 239), COLOR_TEXT_MAIN);
            profileBtn.addActionListener(e -> showEditPopup(this));
            col3.add(profileBtn);

            add(col1); add(col2); add(col3);

            this.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) { selectRow(BankRow.this); }
            });
        }

        public void updateRowImage(String path) {
            this.imagePath = path;
            ImageIcon icon = scaleIcon(path, 32, 32);
            if (icon != null) { imgPlaceholder.setIcon(icon); imgPlaceholder.setText(""); }
            else { imgPlaceholder.setIcon(null); imgPlaceholder.setText("img"); }
        }
    }
}
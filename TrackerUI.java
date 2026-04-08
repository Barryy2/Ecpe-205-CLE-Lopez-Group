import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
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
    private final Color COLOR_TEXT_MUTED = new Color(108, 117, 125);
    private final Color COLOR_GRIDLINE = new Color(210, 210, 210);
    private final Font FONT_MAIN = new Font("SansSerif", Font.PLAIN, 15);
    private final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 15);

    // Profile Tab Components
    private JComboBox<String> bankDropdown;
    private JLabel imagePreviewLabel;

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

        // --- INITIALIZE LIST CONTAINER FIRST ---
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(COLOR_BG);

        // --- BUILD ORIGINAL DASHBOARD PANEL ---
        JPanel dashboardPanel = createOriginalDashboard();

        // --- BUILD NEW PROFILE PANEL ---
        JPanel editProfilePanel = createEditProfilePanel();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tabbedPane.addTab("Dashboard", dashboardPanel);
        tabbedPane.addTab("Edit Profile", editProfilePanel);

        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) refreshBankDropdown();
        });

        add(tabbedPane, BorderLayout.CENTER);

        loadDataIntoUI();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { saveUIStateToDatabase(); }
        });

        setSize(800, 650);
        setLocationRelativeTo(null);
    }

    private JPanel createOriginalDashboard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_GRIDLINE));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(COLOR_BG);

        JPanel headerRow = new JPanel();
        headerRow.setLayout(new BoxLayout(headerRow, BoxLayout.X_AXIS));
        headerRow.add(createHeaderCell("Bank Details", 0, true));
        headerRow.add(createHeaderCell("Amount", COL2_WIDTH, false));
        headerRow.add(createHeaderCell("Actions", COL3_WIDTH, false));
        scrollPane.setColumnHeaderView(headerRow);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(COLOR_BG);
        bottomPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controlsPanel.setBackground(COLOR_BG);
        JButton plusButton = createFlatButton("+ Add Bank", new Color(240, 240, 240), COLOR_TEXT_MAIN);
        plusButton.addActionListener(e -> addBankRow("New Bank", "", "", true));
        JButton minusButton = createFlatButton("- Remove Selected", new Color(255, 240, 240), new Color(200, 50, 50));
        minusButton.addActionListener(e -> {
            if (selectedRow != null) removeBankRow(selectedRow);
        });
        controlsPanel.add(plusButton);
        controlsPanel.add(minusButton);

        JPanel totalsPanel = new JPanel(new BorderLayout());
        totalsPanel.setBackground(COLOR_BG);
        totalsPanel.setBorder(new EmptyBorder(15, 5, 0, 5));
        totalLabel = new JLabel("Total Net: 0.00");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        JButton saveButton = createFlatButton("Save Data", new Color(40, 167, 69), Color.WHITE);
        saveButton.addActionListener(e -> saveUIStateToDatabase());
        totalsPanel.add(totalLabel, BorderLayout.WEST);
        totalsPanel.add(saveButton, BorderLayout.EAST);

        bottomPanel.add(controlsPanel);
        bottomPanel.add(totalsPanel);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createEditProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Assign Images to Banks");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(new JLabel("Select Bank:"), gbc);

        bankDropdown = new JComboBox<>();
        bankDropdown.addActionListener(e -> updatePreview());
        gbc.gridx = 1;
        panel.add(bankDropdown, gbc);

        imagePreviewLabel = new JLabel("No Image", SwingConstants.CENTER);
        imagePreviewLabel.setPreferredSize(new Dimension(100, 100));
        imagePreviewLabel.setBorder(BorderFactory.createLineBorder(COLOR_GRIDLINE));
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(imagePreviewLabel, gbc);

        JButton uploadBtn = createFlatButton("Upload Picture", new Color(0, 123, 255), Color.WHITE);
        uploadBtn.addActionListener(e -> handleUpload());
        gbc.gridy = 3;
        panel.add(uploadBtn, gbc);

        return panel;
    }

    private void refreshBankDropdown() {
        bankDropdown.removeAllItems();
        for (BankRow row : bankRows) bankDropdown.addItem(row.nameField.getText());
        updatePreview();
    }

    private void updatePreview() {
        int idx = bankDropdown.getSelectedIndex();
        if (idx >= 0 && idx < bankRows.size()) {
            String path = bankRows.get(idx).imagePath;
            if (!path.isEmpty()) {
                imagePreviewLabel.setIcon(scaleIcon(path, 100, 100));
                imagePreviewLabel.setText("");
            } else {
                imagePreviewLabel.setIcon(null);
                imagePreviewLabel.setText("No Image");
            }
        }
    }

    private void handleUpload() {
        int idx = bankDropdown.getSelectedIndex();
        if (idx < 0) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            bankRows.get(idx).updateRowImage(path);
            updatePreview();
            saveUIStateToDatabase();
        }
    }

    private ImageIcon scaleIcon(String path, int w, int h) {
        if (path == null || path.isEmpty()) return null;
        try {
            ImageIcon icon = new ImageIcon(path);
            return new ImageIcon(icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Exception e) { return null; }
    }

    // --- RE-USED ORIGINAL UI HELPERS ---

    private JPanel createHeaderCell(String title, int width, boolean stretch) {
        JPanel panel = new JPanel(new BorderLayout());
        if (stretch) {
            panel.setPreferredSize(new Dimension(300, ROW_HEIGHT));
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));
        } else {
            Dimension fixedSize = new Dimension(width, ROW_HEIGHT);
            panel.setPreferredSize(fixedSize);
            panel.setMinimumSize(fixedSize);
            panel.setMaximumSize(fixedSize);
        }
        panel.setBackground(new Color(238, 242, 246));
        panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.WHITE, new Color(180, 180, 180)));
        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JButton createFlatButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setBorder(new EmptyBorder(8, 15, 8, 15));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        return btn;
    }

    private void addBankRow(String name, String amount, String path, boolean startInEditMode) {
        BankRow row = new BankRow(name, amount, path, startInEditMode);
        bankRows.add(row);
        listContainer.add(row);
        selectRow(row);
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
        for (BankRow row : bankRows) amounts.add(row.amountField.getText());
        double total = calcLogic.computeTotalAssets(amounts);
        totalLabel.setText(String.format("Total Net: %,.2f", total));
    }

    private void loadDataIntoUI() {
        List<String[]> accounts = dbManager.loadAccounts();
        if (accounts.isEmpty()) {
            addBankRow("Bank 1", "", "", true);
            addBankRow("Bank 2", "", "", true);
        } else {
            for (String[] acc : accounts) {
                addBankRow(acc[0], acc[1], acc.length > 2 ? acc[2] : "", false);
            }
        }
        selectRow(null);
    }

    private void saveUIStateToDatabase() {
        List<String[]> dataToSave = new ArrayList<>();
        for (BankRow row : bankRows) {
            dataToSave.add(new String[]{row.nameField.getText(), row.amountField.getText(), row.imagePath});
        }
        try { dbManager.saveAccounts(dataToSave); } catch (Exception ignored) {}
    }

    // --- RESTORED BANK ROW (Exactly like your original) ---

    private class BankRow extends JPanel {
        JTextField nameField, amountField;
        JButton editButton;
        JLabel imgPlaceholder;
        String imagePath;
        boolean isEditing;

        public BankRow(String name, String amount, String path, boolean startInEditMode) {
            this.imagePath = path;
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setBackground(COLOR_BG);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));

            // COL 1
            JPanel col1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
            col1.setOpaque(false);
            col1.setPreferredSize(new Dimension(300, ROW_HEIGHT));
            col1.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));
            col1.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, COLOR_GRIDLINE));

            imgPlaceholder = new JLabel("", SwingConstants.CENTER);
            imgPlaceholder.setPreferredSize(new Dimension(32, 32));
            imgPlaceholder.setOpaque(true);
            imgPlaceholder.setBackground(new Color(240, 240, 240));
            updateRowImage(path);

            nameField = new JTextField(name);
            nameField.setFont(FONT_MAIN);
            nameField.setOpaque(false);
            nameField.setPreferredSize(new Dimension(220, 30));

            col1.add(imgPlaceholder);
            col1.add(nameField);

            // COL 2
            JPanel col2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
            col2.setOpaque(false);
            Dimension col2Size = new Dimension(COL2_WIDTH, ROW_HEIGHT);
            col2.setPreferredSize(col2Size);
            col2.setMinimumSize(col2Size);
            col2.setMaximumSize(col2Size);
            col2.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, COLOR_GRIDLINE));

            amountField = new JTextField(amount);
            amountField.setFont(FONT_BOLD);
            amountField.setHorizontalAlignment(JTextField.RIGHT);
            amountField.setOpaque(false);
            amountField.setPreferredSize(new Dimension(110, 30));
            amountField.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { calculateTotal(); }
                public void removeUpdate(DocumentEvent e) { calculateTotal(); }
                public void changedUpdate(DocumentEvent e) { calculateTotal(); }
            });
            col2.add(amountField);

            // COL 3
            JPanel col3 = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 8));
            col3.setOpaque(false);
            Dimension col3Size = new Dimension(COL3_WIDTH, ROW_HEIGHT);
            col3.setPreferredSize(col3Size);
            col3.setMinimumSize(col3Size);
            col3.setMaximumSize(col3Size);
            col3.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_GRIDLINE));

            editButton = new JButton();
            editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            editButton.setContentAreaFilled(false);
            editButton.setOpaque(true);
            editButton.addActionListener(e -> toggleEditMode(!isEditing));
            col3.add(editButton);

            add(col1); add(col2); add(col3);
            toggleEditMode(startInEditMode);

            MouseAdapter mouseAction = new MouseAdapter() {
                public void mousePressed(MouseEvent e) { selectRow(BankRow.this); }
                public void mouseEntered(MouseEvent e) { if (selectedRow != BankRow.this) setBackground(COLOR_HOVER); }
                public void mouseExited(MouseEvent e) { if (selectedRow != BankRow.this) setBackground(COLOR_BG); }
            };
            this.addMouseListener(mouseAction);
            col1.addMouseListener(mouseAction);
            col2.addMouseListener(mouseAction);
            col3.addMouseListener(mouseAction);
        }

        public void updateRowImage(String path) {
            this.imagePath = (path == null) ? "" : path;
            ImageIcon icon = scaleIcon(this.imagePath, 32, 32);
            if (icon != null) {
                imgPlaceholder.setIcon(icon);
                imgPlaceholder.setText("");
            } else {
                imgPlaceholder.setIcon(null);
                imgPlaceholder.setText("img");
            }
        }

        private void toggleEditMode(boolean enable) {
            isEditing = enable;
            nameField.setEditable(enable);
            amountField.setEditable(enable);
            if (enable) {
                editButton.setText("Done");
                editButton.setBackground(new Color(212, 237, 218));
                nameField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(150, 150, 150)));
                amountField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(150, 150, 150)));
            } else {
                editButton.setText("Edit");
                editButton.setBackground(new Color(233, 236, 239));
                nameField.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
                amountField.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
            }
        }
    }
}
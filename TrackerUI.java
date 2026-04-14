import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class TrackerUI extends JFrame {

    private final JPanel listContainer;
    private final List<BankRow> bankRows = new ArrayList<>();
    private JLabel totalLabel;
    private BankRow selectedRow = null;

    // Database and Logic instances
    private final DatabaseManager dbManager;
    private final CalculationsLogic calcLogic;

    // Standardized Sizes - We only lock the right-side columns now.
    // The first column will be allowed to stretch dynamically!
    private final int ROW_HEIGHT = 50;
    private final int COL2_WIDTH = 150; // Amount
    private final int COL3_WIDTH = 150; // Actions

    // Color Palette
    private final Color COLOR_BG = Color.WHITE;
    private final Color COLOR_HOVER = new Color(245, 248, 250);
    private final Color COLOR_SELECTED = new Color(230, 238, 245);
    private final Color COLOR_TEXT_MAIN = new Color(33, 37, 41);
    private final Color COLOR_TEXT_MUTED = new Color(108, 117, 125);
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

        // --- DASHBOARD PANEL ---
        JPanel dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBackground(Color.GREEN);
        dashboardPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        //CENTER: Scrollable Table Data
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(Color.BLUE);

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_GRIDLINE));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        //scrollPane.getViewport().setBackground(Color.RED);

        // --- BUILD THE TABLE HEADER ---
        JPanel headerRow = new JPanel();
        headerRow.setLayout(new BoxLayout(headerRow, BoxLayout.X_AXIS));
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Headers
        headerRow.add(createHeaderCell("Bank Details", 0, true));
        headerRow.add(createHeaderCell("Amount", COL2_WIDTH, false));
        headerRow.add(createHeaderCell("Actions", COL3_WIDTH, false));

        scrollPane.setColumnHeaderView(headerRow);

        // --- SOUTH: Controls and Totals ---
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(COLOR_BG);
        bottomPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controlsPanel.setBackground(COLOR_BG);

        JButton plusButton = createFlatButton("+ Add Bank", new Color(240, 240, 240), COLOR_TEXT_MAIN);
        plusButton.addActionListener(e -> {
            addBankRow("New Bank", "", true);
            revalidate();
            repaint();
        });

        JButton minusButton = createFlatButton("- Remove Selected", new Color(255, 240, 240), new Color(200, 50, 50));
        minusButton.addActionListener(e -> {
            if (selectedRow != null) {
                removeBankRow(selectedRow);
            } else if (!bankRows.isEmpty()) {
                removeBankRow(bankRows.get(bankRows.size() - 1));
            }
        });

        controlsPanel.add(plusButton);
        controlsPanel.add(minusButton);

        JPanel totalsPanel = new JPanel(new BorderLayout());
        totalsPanel.setBackground(COLOR_BG);
        totalsPanel.setBorder(new EmptyBorder(15, 5, 0, 5));

        totalLabel = new JLabel("Total Net: 0.00");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        totalLabel.setForeground(COLOR_TEXT_MAIN);

        JButton saveButton = createFlatButton("Save Data", new Color(40, 167, 69), Color.WHITE);
        saveButton.addActionListener(e -> saveUIStateToDatabase());

        totalsPanel.add(totalLabel, BorderLayout.WEST);
        totalsPanel.add(saveButton, BorderLayout.EAST);

        bottomPanel.add(controlsPanel);
        bottomPanel.add(totalsPanel);

        dashboardPanel.add(scrollPane, BorderLayout.CENTER);
        dashboardPanel.add(bottomPanel, BorderLayout.SOUTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tabbedPane.setFocusable(false);
        tabbedPane.addTab("Dashboard", dashboardPanel);

        add(tabbedPane, BorderLayout.CENTER);

        loadDataIntoUI();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveUIStateToDatabase();
            }
        });

        setSize(800, 650); // Slightly wider default to show off the responsive layout
        setLocationRelativeTo(null);
    }

    private JPanel createHeaderCell(String title, int width, boolean stretch) {
        JPanel panel = new JPanel(new BorderLayout());

        if (stretch) {
            panel.setPreferredSize(new Dimension(300, ROW_HEIGHT));
            // MAX_VALUE allows this specific column to expand infinitely to fill empty space
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
        label.setForeground(new Color(50, 50, 50));
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

    private void selectRow(BankRow row) {
        if (selectedRow != null) selectedRow.setBackground(COLOR_BG);
        selectedRow = row;
        if (selectedRow != null) selectedRow.setBackground(COLOR_SELECTED);
    }

    private void addBankRow(String name, String amount, boolean startInEditMode) {
        BankRow row = new BankRow(name, amount, startInEditMode);
        bankRows.add(row);
        listContainer.add(row);
        selectRow(row);
        calculateTotal();
    }

    private void removeBankRow(BankRow row) {
        bankRows.remove(row);
        listContainer.remove(row);
        if (selectedRow == row) selectedRow = null;
        calculateTotal();
        listContainer.revalidate();
        listContainer.repaint();
    }

    private void calculateTotal() {
        List<String> amountStrings = new ArrayList<>();
        for (BankRow row : bankRows) {
            amountStrings.add(row.amountField.getText());
        }
        double total = calcLogic.computeTotalAssets(amountStrings);
        totalLabel.setText(String.format("Total Net: %,.2f", total));
    }

    private void loadDataIntoUI() {
        List<String[]> accounts = dbManager.loadAccounts();

        if (accounts.isEmpty()) {
            addBankRow("Bank 1", "", true);
            addBankRow("Bank 2", "", true);
        } else {
            for (String[] acc : accounts) {
                addBankRow(acc[0], acc[1], false);
            }
        }
        selectRow(null);
    }

    private void saveUIStateToDatabase() {
        this.requestFocusInWindow();
        List<String[]> dataToSave = new ArrayList<>();
        for (BankRow row : bankRows) {
            String[] accountData = {
                    row.nameField.getText().trim(),
                    row.amountField.getText().trim()
            };
            dataToSave.add(accountData);
        }
        try {
            dbManager.saveAccounts(dataToSave);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving data: " + e.getMessage());
        }
    }

    // --- CUSTOM ROW COMPONENT ---

    private class BankRow extends JPanel {
        JTextField nameField;
        JTextField amountField;
        JButton editButton;
        boolean isEditing;

        public BankRow(String name, String amount, boolean startInEditMode) {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setBackground(COLOR_BG);

            // Limit maximum height of entire row so they don't stretch vertically
            setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));

            // --- COLUMN 1: IMAGE + NAME (STRETCHES) ---
            JPanel col1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
            col1.setOpaque(false);
            col1.setPreferredSize(new Dimension(300, ROW_HEIGHT));
            col1.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT)); // Expand dynamically!
            col1.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, COLOR_GRIDLINE));

            JLabel imgPlaceholder = new JLabel("img", SwingConstants.CENTER);
            imgPlaceholder.setPreferredSize(new Dimension(32, 32));
            imgPlaceholder.setOpaque(true);
            imgPlaceholder.setBackground(new Color(240, 240, 240));
            imgPlaceholder.setForeground(COLOR_TEXT_MUTED);
            imgPlaceholder.setFont(new Font("SansSerif", Font.PLAIN, 10));

            nameField = new JTextField(name);
            nameField.setFont(FONT_MAIN);
            nameField.setForeground(COLOR_TEXT_MAIN);
            nameField.setOpaque(false);
            nameField.setPreferredSize(new Dimension(220, 30));

            col1.add(imgPlaceholder);
            col1.add(nameField);

            // --- COLUMN 2: AMOUNT (LOCKED WIDTH) ---
            JPanel col2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
            col2.setOpaque(false);
            Dimension col2Size = new Dimension(COL2_WIDTH, ROW_HEIGHT);
            col2.setPreferredSize(col2Size);
            col2.setMinimumSize(col2Size);
            col2.setMaximumSize(col2Size);
            col2.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, COLOR_GRIDLINE));

            amountField = new JTextField(amount);
            amountField.setFont(FONT_BOLD);
            amountField.setForeground(COLOR_TEXT_MAIN);
            amountField.setOpaque(false);
            amountField.setHorizontalAlignment(JTextField.RIGHT);
            amountField.setPreferredSize(new Dimension(110, 30));

            amountField.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { calculateTotal(); }
                public void removeUpdate(DocumentEvent e) { calculateTotal(); }
                public void changedUpdate(DocumentEvent e) { calculateTotal(); }
            });

            col2.add(amountField);

            // --- COLUMN 3: EDIT BUTTON (LOCKED WIDTH) ---
            JPanel col3 = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 8));
            col3.setOpaque(false);
            Dimension col3Size = new Dimension(COL3_WIDTH, ROW_HEIGHT);
            col3.setPreferredSize(col3Size);
            col3.setMinimumSize(col3Size);
            col3.setMaximumSize(col3Size);
            col3.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_GRIDLINE));

            editButton = new JButton();
            editButton.setFont(new Font("SansSerif", Font.BOLD, 12));
            editButton.setBorder(new EmptyBorder(5, 15, 5, 15));
            editButton.setFocusPainted(false);
            editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            editButton.setContentAreaFilled(false);
            editButton.setOpaque(true);

            editButton.addActionListener(e -> toggleEditMode(!isEditing));

            col3.add(editButton);

            add(col1);
            add(col2);
            add(col3);

            toggleEditMode(startInEditMode);

            // Hover and Selection logic.
            MouseAdapter mouseAction = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) { selectRow(BankRow.this); }
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (selectedRow != BankRow.this) setBackground(COLOR_HOVER);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (selectedRow != BankRow.this) setBackground(COLOR_BG);
                }
            };

            this.addMouseListener(mouseAction);
            col1.addMouseListener(mouseAction);
            col2.addMouseListener(mouseAction);
            col3.addMouseListener(mouseAction);

            FocusAdapter focusAction = new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) { selectRow(BankRow.this); }
            };
            nameField.addFocusListener(focusAction);
            amountField.addFocusListener(focusAction);
        }

        private void toggleEditMode(boolean enable) {
            isEditing = enable;
            nameField.setEditable(enable);
            amountField.setEditable(enable);

            if (enable) {
                editButton.setText("Done");
                editButton.setBackground(new Color(212, 237, 218));
                editButton.setForeground(new Color(21, 87, 36));

                nameField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(150, 150, 150)));
                amountField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(150, 150, 150)));
                nameField.requestFocusInWindow();
            } else {
                editButton.setText("Edit");
                editButton.setBackground(new Color(233, 236, 239));
                editButton.setForeground(COLOR_TEXT_MAIN);

                nameField.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
                amountField.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
            }
        }
    }
}
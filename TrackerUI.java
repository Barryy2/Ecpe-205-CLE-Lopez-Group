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

    // Standardized Sizes
    private final int ROW_HEIGHT = 50;
    private final int COL2_WIDTH = 150; // Amount
    private final int COL3_WIDTH = 230; // Actions

    // Fonts & Colors Palette
    private final String FONT_FAMILY = "Segoe UI";

    private final Color COLOR_BG = Color.WHITE;
    private final Color COLOR_HOVER = new Color(245, 248, 250);
    private final Color COLOR_SELECTED = new Color(230, 238, 245);
    private final Color COLOR_TEXT_MAIN = new Color(33, 37, 41);
    private final Color COLOR_TEXT_MUTED = new Color(108, 117, 125);
    private final Color COLOR_GRIDLINE = new Color(210, 210, 210);

    private final Font FONT_MAIN = new Font(FONT_FAMILY, Font.PLAIN, 15);
    private final Font FONT_BOLD = new Font(FONT_FAMILY, Font.BOLD, 15);

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

        // DASHBOARD PANEL
        JPanel dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBackground(COLOR_BG);
        dashboardPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // CENTER: Scrollable Table Data
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setOpaque(false);

        //
        JPanel backgroundWrapper = new JPanel(new BorderLayout());
        backgroundWrapper.setBackground(COLOR_BG);
        backgroundWrapper.add(listContainer, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(backgroundWrapper);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_GRIDLINE));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        //  BUILD THE TABLE HEADER
        JPanel headerRow = new JPanel();
        headerRow.setLayout(new BoxLayout(headerRow, BoxLayout.X_AXIS));
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerRow.add(createHeaderCell("Bank Details", 0, true));
        headerRow.add(createHeaderCell("Amount", COL2_WIDTH, false));
        headerRow.add(createHeaderCell("Actions", COL3_WIDTH, false));

        scrollPane.setColumnHeaderView(headerRow);

        //  SOUTH Controls and Totals
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(COLOR_BG);
        bottomPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controlsPanel.setBackground(COLOR_BG);

        JButton plusButton = createFlatButton("+ Add Bank", new Color(240, 240, 240), COLOR_TEXT_MAIN);
        plusButton.addActionListener(e -> addBankRow("New Bank", "0.00", "", true));

        JButton minusButton = createFlatButton("- Remove Selected", new Color(255, 240, 240), new Color(200, 50, 50));
        minusButton.addActionListener(e -> {
            if (selectedRow != null) {
                removeBankRow(selectedRow);
            }
        });

        controlsPanel.add(plusButton);
        controlsPanel.add(minusButton);

        JPanel totalsPanel = new JPanel(new BorderLayout());
        totalsPanel.setBackground(COLOR_BG);
        totalsPanel.setBorder(new EmptyBorder(15, 5, 0, 5));

        totalLabel = new JLabel("Total Net: 0.00");
        totalLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 18));
        totalLabel.setForeground(COLOR_TEXT_MAIN);

        JButton saveButton = createFlatButton("Save All Data", new Color(40, 167, 69), Color.WHITE);
        saveButton.addActionListener(e -> saveUIStateToDatabase());

        totalsPanel.add(totalLabel, BorderLayout.WEST);
        totalsPanel.add(saveButton, BorderLayout.EAST);

        bottomPanel.add(controlsPanel);
        bottomPanel.add(totalsPanel);

        dashboardPanel.add(scrollPane, BorderLayout.CENTER);
        dashboardPanel.add(bottomPanel, BorderLayout.SOUTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font(FONT_FAMILY, Font.PLAIN, 13));
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

        setSize(850, 650);
        setLocationRelativeTo(null);
    }

    //  UI HELPERS

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
        label.setFont(new Font(FONT_FAMILY, Font.BOLD, 13));
        label.setForeground(new Color(50, 50, 50));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JButton createFlatButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(FONT_FAMILY, Font.BOLD, 12));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    private ImageIcon scaleIcon(String path, int w, int h) {
        if (path == null || path.isEmpty()) return null;
        try {
            return new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Exception e) { return null; }
    }

    private void showImagePopup(BankRow row) {
        JDialog dialog = new JDialog(this, "Update Icon", true);
        dialog.setLayout(new FlowLayout());
        dialog.getContentPane().setBackground(COLOR_BG);

        JLabel preview = new JLabel("", SwingConstants.CENTER);
        preview.setPreferredSize(new Dimension(100, 100));
        preview.setBorder(BorderFactory.createLineBorder(COLOR_GRIDLINE));

        ImageIcon currentIcon = scaleIcon(row.imagePath, 100, 100);
        if (currentIcon != null) {
            preview.setIcon(currentIcon);
        } else {
            preview.setText("No Image");
        }

        // Quick Select Buttons
        String gcashPath = "Default Bank Images/Gcash.png";
        JButton gcashBtn = new JButton(scaleIcon(gcashPath, 100, 100));
        gcashBtn.addActionListener(e -> { row.updateRowImage(gcashPath); dialog.dispose(); });

        String gotymePath = "Default Bank Images/GoTyme.png";
        JButton gotymeBtn = new JButton(scaleIcon(gotymePath, 100, 100));
        gotymeBtn.addActionListener(e -> { row.updateRowImage(gotymePath); dialog.dispose(); });

        String mariBankPath = "Default Bank Images/MariBank.png";
        JButton mariBankBtn = new JButton(scaleIcon(mariBankPath, 100, 100));
        mariBankBtn.addActionListener(e -> { row.updateRowImage(mariBankPath); dialog.dispose(); });

        String payMayaPath = "Default Bank Images/PayMaya.png";
        JButton mayaBtn = new JButton(scaleIcon(payMayaPath, 100, 100));
        mayaBtn.addActionListener(e -> { row.updateRowImage(payMayaPath); dialog.dispose(); });

        JButton upload = createFlatButton("Select Image File", new Color(0, 123, 255), Color.WHITE);
        upload.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                String filePath = fc.getSelectedFile().getAbsolutePath();

                verifyFileImage.VerificationResult result = verifyFileImage.verifyImageFile(filePath);
                if (result.isValid) {
                    row.updateRowImage(filePath);
                    JOptionPane.showMessageDialog(dialog, result.message, "Image Verified", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, result.message, "Invalid Image File", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dialog.add(preview);
        dialog.add(gcashBtn);
        dialog.add(mayaBtn);
        dialog.add(mariBankBtn);
        dialog.add(gotymeBtn);
        dialog.add(upload);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    //  DATA & LOGIC HANDLING

    private void addBankRow(String name, String amount, String path, boolean startInEditMode) {
        BankRow row = new BankRow(name, amount, path, true);
        bankRows.add(row);
        listContainer.add(row);
        selectRow(row);
        calculateTotal();
        listContainer.revalidate();
        listContainer.repaint();
    }

    private void removeBankRow(BankRow row) {
        bankRows.remove(row);

        if (row.isMainRow) {
            for (Component child : row.childrenPanel.getComponents()) {
                if (child instanceof BankRow) {
                    bankRows.remove((BankRow) child);
                }
            }
            listContainer.remove(row);
        } else {
            Container parent = row.getParent();
            if (parent != null) parent.remove(row);
        }

        if (selectedRow == row) selectedRow = null;
        calculateTotal();
        listContainer.revalidate();
        listContainer.repaint();
    }

    private void selectRow(BankRow row) {
        if (selectedRow != null) selectedRow.setSelection(false);
        selectedRow = row;
        if (selectedRow != null) selectedRow.setSelection(true);
    }

    private void calculateTotal() {
        List<String> amounts = new ArrayList<>();
        for (BankRow r : bankRows) {
            amounts.add(r.amountField.getText());
        }
        totalLabel.setText(String.format("Total Net: %,.2f", calcLogic.computeTotalAssets(amounts)));
    }

    private void loadDataIntoUI() {
        List<String[]> data = dbManager.loadAccounts();
        if (data.isEmpty()) {
            addBankRow("Bank 1", "0.00", "", false);
        } else {
            BankRow currentMain = null;
            for (String[] row : data) {
                String parentId = row[1];
                if (parentId.equals("0")) {
                    currentMain = new BankRow(row[2], row[3], row[4], true);
                    bankRows.add(currentMain);
                    listContainer.add(currentMain);
                } else if (currentMain != null) {
                    currentMain.addSubBank(row[2], row[3], row[4]);
                }
            }
        }
        selectRow(null);
        calculateTotal();
        listContainer.revalidate();
        listContainer.repaint();
    }

    private void saveUIStateToDatabase() {
        this.requestFocusInWindow();
        List<String[]> toSave = new ArrayList<>();
        int idCounter = 1;

        for (Component c : listContainer.getComponents()) {
            if (c instanceof BankRow) {
                BankRow mainRow = (BankRow) c;
                int mainId = idCounter++;
                toSave.add(new String[]{
                        String.valueOf(mainId), "0", mainRow.nameField.getText().trim(),
                        mainRow.amountField.getText().trim(), mainRow.imagePath
                });

                for (Component child : mainRow.childrenPanel.getComponents()) {
                    if (child instanceof BankRow) {
                        BankRow subRow = (BankRow) child;
                        int subId = idCounter++;
                        toSave.add(new String[]{
                                String.valueOf(subId), String.valueOf(mainId), subRow.nameField.getText().trim(),
                                subRow.amountField.getText().trim(), subRow.imagePath
                        });
                    }
                }
            }
        }
        try {
            dbManager.saveAccounts(toSave);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving data: " + e.getMessage());
        }
    }

    //  CUSTOM HIERARCHICAL ROW COMPONENT

    private class BankRow extends JPanel {
        JTextField nameField, amountField;
        JLabel imgPlaceholder;
        JButton editBtn, toggleBtn;
        String imagePath;
        boolean isEditing = false;
        boolean isMainRow;
        boolean isExpanded = false;

        JPanel headerPanel;
        JPanel childrenPanel;

        public BankRow(String name, String amount, String path, boolean isMainRow) {
            this.imagePath = path;
            this.isMainRow = isMainRow;

            setLayout(new BorderLayout());
            setBackground(COLOR_BG);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, isMainRow ? ROW_HEIGHT * 10 : ROW_HEIGHT));

            headerPanel = new JPanel();
            headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
            headerPanel.setBackground(COLOR_BG);
            headerPanel.setPreferredSize(new Dimension(0, isMainRow ? ROW_HEIGHT : ROW_HEIGHT - 10));
            headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, isMainRow ? ROW_HEIGHT : ROW_HEIGHT - 10));

            int verticalGap = isMainRow ? 8 : 4;

            // --- COLUMN 1: TOGGLE, IMAGE, NAME ---
            JPanel col1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, verticalGap));
            col1.setOpaque(false);

            if (!isMainRow) {
                col1.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, COLOR_GRIDLINE),
                        BorderFactory.createEmptyBorder(0, 55, 0, 0) // Indent sub-banks
                ));
            } else {
                col1.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, COLOR_GRIDLINE));

                // NEW: Use an empty string and rely entirely on our custom-drawn shape
                toggleBtn = createFlatButton("", COLOR_BG, COLOR_TEXT_MAIN);
                toggleBtn.setIcon(new ArrowIcon(false));
                toggleBtn.setPreferredSize(new Dimension(45, 30));
                toggleBtn.addActionListener(e -> toggleExpand());
                col1.add(toggleBtn);
            }

            imgPlaceholder = new JLabel("", SwingConstants.CENTER);
            imgPlaceholder.setPreferredSize(new Dimension(32, 32));
            imgPlaceholder.setOpaque(true);
            imgPlaceholder.setBackground(new Color(240, 240, 240));
            imgPlaceholder.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (isEditing) showImagePopup(BankRow.this);
                }
            });
            updateRowImage(path);

            nameField = new JTextField(name);
            nameField.setPreferredSize(new Dimension(160, 30));
            nameField.setFont(FONT_MAIN);
            nameField.setForeground(COLOR_TEXT_MAIN);
            nameField.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
            nameField.setOpaque(false);
            nameField.setEditable(false);

            col1.add(imgPlaceholder);
            col1.add(nameField);

            //  COLUMN 2: AMOUNT
            JPanel col2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, verticalGap));
            col2.setOpaque(false);
            Dimension d2 = new Dimension(COL2_WIDTH, isMainRow ? ROW_HEIGHT : ROW_HEIGHT - 10);
            col2.setPreferredSize(d2); col2.setMaximumSize(d2);
            col2.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, COLOR_GRIDLINE));

            amountField = new JTextField(amount);
            amountField.setPreferredSize(new Dimension(110, 30));
            amountField.setHorizontalAlignment(JTextField.RIGHT);
            amountField.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
            amountField.setOpaque(false);
            amountField.setEditable(false);
            amountField.setFont(FONT_BOLD);
            amountField.setForeground(COLOR_TEXT_MAIN);

            amountField.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { calculateTotal(); }
                public void removeUpdate(DocumentEvent e) { calculateTotal(); }
                public void changedUpdate(DocumentEvent e) { calculateTotal(); }
            });

            col2.add(amountField);

            // --- COLUMN 3: ACTIONS ---
            JPanel col3 = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, verticalGap));
            col3.setOpaque(false);
            Dimension d3 = new Dimension(COL3_WIDTH, isMainRow ? ROW_HEIGHT : ROW_HEIGHT - 10);
            col3.setPreferredSize(d3); col3.setMaximumSize(d3);
            col3.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_GRIDLINE));

            editBtn = createFlatButton("Edit", new Color(233, 236, 239), COLOR_TEXT_MAIN);
            editBtn.setPreferredSize(new Dimension(70, 28));
            editBtn.addActionListener(e -> toggleEditMode());
            col3.add(editBtn);

            if (isMainRow) {
                JButton addSubBtn = createFlatButton("+ Sub", new Color(230, 245, 230), new Color(40, 167, 69));
                addSubBtn.setPreferredSize(new Dimension(70, 28));
                addSubBtn.addActionListener(e -> {
                    addSubBank("New Sub", "0.00", BankRow.this.imagePath);
                    if (!isExpanded) toggleExpand();
                });
                col3.add(addSubBtn);
            }

            headerPanel.add(col1);
            headerPanel.add(col2);
            headerPanel.add(col3);

            add(headerPanel, BorderLayout.NORTH);

            // --- CHILDREN CONTAINER ---
            childrenPanel = new JPanel();
            childrenPanel.setLayout(new BoxLayout(childrenPanel, BoxLayout.Y_AXIS));
            childrenPanel.setOpaque(false);
            childrenPanel.setVisible(false);
            add(childrenPanel, BorderLayout.CENTER);

            // --- HOVER & SELECTION LOGIC ---
            MouseAdapter mouseAction = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) { selectRow(BankRow.this); }
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (selectedRow != BankRow.this) headerPanel.setBackground(COLOR_HOVER);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (selectedRow != BankRow.this) headerPanel.setBackground(COLOR_BG);
                }
            };

            headerPanel.addMouseListener(mouseAction);
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

        public void addSubBank(String name, String amount, String path) {
            BankRow subRow = new BankRow(name, amount, path, false);
            bankRows.add(subRow);
            childrenPanel.add(subRow);
            calculateTotal();
            revalidate();
            repaint();
        }

        public void toggleExpand() {
            if (!isMainRow) return;
            isExpanded = !isExpanded;
            childrenPanel.setVisible(isExpanded);
            // NEW: Swaps the drawn icon depending on state
            toggleBtn.setIcon(new ArrowIcon(isExpanded));
            revalidate();
        }

        public void updateRowImage(String path) {
            this.imagePath = path;
            ImageIcon icon = scaleIcon(path, 24, 24);
            if (icon != null) {
                imgPlaceholder.setIcon(icon);
                imgPlaceholder.setText("");
            } else {
                imgPlaceholder.setIcon(null);
                imgPlaceholder.setText("img");
            }
        }

        public void setSelection(boolean selected) {
            headerPanel.setBackground(selected ? COLOR_SELECTED : COLOR_BG);
        }

        private void toggleEditMode() {
            isEditing = !isEditing;
            nameField.setEditable(isEditing);
            amountField.setEditable(isEditing);

            if (isEditing) {
                editBtn.setText("Done");
                editBtn.setBackground(new Color(212, 237, 218));
                editBtn.setForeground(new Color(21, 87, 36));

                nameField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(150, 150, 150)));
                amountField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(150, 150, 150)));
                nameField.requestFocusInWindow();
            } else {
                editBtn.setText("Edit");
                editBtn.setBackground(new Color(233, 236, 239));
                editBtn.setForeground(COLOR_TEXT_MAIN);

                nameField.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
                amountField.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
            }
        }
    }

    // --- NEW CLASS: FONT-INDEPENDENT DRAWN ARROW ---
    private class ArrowIcon implements Icon {
        private final boolean isExpanded;

        public ArrowIcon(boolean isExpanded) {
            this.isExpanded = isExpanded;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            // Turns on anti-aliasing so the arrow has smooth edges
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COLOR_TEXT_MAIN);

            // Shifting x and y slightly to center it inside the button
            int shiftX = x + 3;
            int shiftY = y + 2;

            if (isExpanded) {
                // Draws a Down-Pointing Triangle ▼
                int[] xPoints = {shiftX, shiftX + 10, shiftX + 5};
                int[] yPoints = {shiftY + 3, shiftY + 3, shiftY + 9};
                g2.fillPolygon(xPoints, yPoints, 3);
            } else {
                // Draws a Right-Pointing Triangle ▶
                int[] xPoints = {shiftX + 2, shiftX + 8, shiftX + 2};
                int[] yPoints = {shiftY, shiftY + 5, shiftY + 10};
                g2.fillPolygon(xPoints, yPoints, 3);
            }
            g2.dispose();
        }

        @Override public int getIconWidth() { return 14; }
        @Override public int getIconHeight() { return 14; }
    }
}
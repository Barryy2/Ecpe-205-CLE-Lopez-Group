import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
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
    private final int COL3_WIDTH = 230;

    private final Color COLOR_BG = Color.WHITE;
    private final Color COLOR_SELECTED = new Color(230, 238, 245);
    private final Color COLOR_TEXT_MAIN = new Color(33, 37, 41);
    private final Color COLOR_GRIDLINE = new Color(210, 210, 210);
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

        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(COLOR_BG);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 13));

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

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(COLOR_BG);
        bottomPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.setBackground(COLOR_BG);
        JButton addBtn = createFlatButton("+ Add Bank", new Color(240, 240, 240), COLOR_TEXT_MAIN);
        addBtn.addActionListener(e -> addBankRow("New Bank", "0.00", "", true));

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

        JButton saveBtn = createFlatButton("Save All Data", new Color(40, 167, 69), Color.WHITE);
        saveBtn.addActionListener(e -> saveUIStateToDatabase());

        totalsPanel.add(totalLabel, BorderLayout.WEST);
        totalsPanel.add(saveBtn, BorderLayout.EAST);

        bottomPanel.add(controls);
        bottomPanel.add(totalsPanel);
        tablePanel.add(bottomPanel, BorderLayout.SOUTH);

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


        String gcashPath = "Default Bank Images/Gcash.png";
        ImageIcon gcashIcon = scaleIcon(gcashPath, 100, 100);
        JButton gcashBtn = new JButton(gcashIcon);
        gcashBtn.addActionListener(e -> {
            row.updateRowImage(gcashPath);
            dialog.dispose();
        });

        String gotymePath = "Default Bank Images/GoTyme.png";
        ImageIcon gotymeIcon = scaleIcon(gotymePath, 100, 100);
        JButton gotymeButton = new JButton(gotymeIcon);
        gotymeButton.addActionListener(e -> {
            row.updateRowImage(gotymePath);
            dialog.dispose();
        });

        String mariBankPath = "Default Bank Images/MariBank.png";
        ImageIcon mariBankIcon = scaleIcon(mariBankPath, 100, 100);
        JButton mariBankBtn = new JButton(mariBankIcon);
        mariBankBtn.addActionListener(e -> {
            row.updateRowImage(mariBankPath);
            dialog.dispose();
        });

        String payMayaPath = "Default Bank Images/PayMaya.png";
        ImageIcon mayaIcon = scaleIcon(payMayaPath, 100, 100);
        JButton mayaBtn = new JButton(mayaIcon);
        mayaBtn.addActionListener(e -> {
            row.updateRowImage(payMayaPath);
            dialog.dispose();
        });



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
        dialog.add(gotymeButton);
        dialog.add(upload);

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
        b.setFont(new Font("SansSerif", Font.BOLD, 11));
        b.setBackground(bg); b.setForeground(fg);
        b.setOpaque(true); b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));

        b.setMargin(new Insets(0, 0, 0, 0));
        return b;
    }

    private void addBankRow(String name, String amount, String path, boolean edit) {
        BankRow row = new BankRow(name, amount, path, true);
        bankRows.add(row);
        listContainer.add(row);
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
            if (parent != null) {
                parent.remove(row);
            }
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
        for (BankRow r : bankRows) amounts.add(r.amountField.getText());
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
        calculateTotal();
        listContainer.revalidate();
        listContainer.repaint();
    }

    private void saveUIStateToDatabase() {
        List<String[]> toSave = new ArrayList<>();
        int idCounter = 1;

        for (Component c : listContainer.getComponents()) {
            if (c instanceof BankRow) {
                BankRow mainRow = (BankRow) c;
                int mainId = idCounter++;
                toSave.add(new String[]{
                        String.valueOf(mainId), "0", mainRow.nameField.getText(), mainRow.amountField.getText(), mainRow.imagePath
                });

                for (Component child : mainRow.childrenPanel.getComponents()) {
                    if (child instanceof BankRow) {
                        BankRow subRow = (BankRow) child;
                        int subId = idCounter++;
                        toSave.add(new String[]{
                                String.valueOf(subId), String.valueOf(mainId), subRow.nameField.getText(), subRow.amountField.getText(), subRow.imagePath
                        });
                    }
                }
            }
        }
        try { dbManager.saveAccounts(toSave); } catch (Exception ignored) {}
    }

    private class BankRow extends JPanel {
        JTextField nameField, amountField;
        JLabel imgPlaceholder;
        JButton editBtn;
        JButton toggleBtn;
        String imagePath;
        boolean isEditing = false;
        boolean isMainRow;

        JPanel headerPanel;
        JPanel childrenPanel;
        boolean isExpanded = false;

        public BankRow(String name, String amount, String path, boolean isMainRow) {
            this.imagePath = path;
            this.isMainRow = isMainRow;

            setLayout(new BorderLayout());
            setBackground(COLOR_BG);

            headerPanel = new JPanel();
            headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
            headerPanel.setBackground(COLOR_BG);

            headerPanel.setPreferredSize(new Dimension(0, isMainRow ? ROW_HEIGHT : ROW_HEIGHT - 10));
            headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, isMainRow ? ROW_HEIGHT : ROW_HEIGHT - 10));

            int verticalGap = isMainRow ? 8 : 4;

            JPanel col1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, verticalGap));
            col1.setOpaque(false);

            if (!isMainRow) {
                col1.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, COLOR_GRIDLINE),
                        BorderFactory.createEmptyBorder(0, 55, 0, 0)
                ));
            } else {
                col1.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, COLOR_GRIDLINE));
                toggleBtn = createFlatButton("▶", COLOR_BG, COLOR_TEXT_MAIN);
                toggleBtn.setPreferredSize(new Dimension(45, 30));
                toggleBtn.addActionListener(e -> toggleExpand());
                col1.add(toggleBtn);
            }

            imgPlaceholder = new JLabel("", SwingConstants.CENTER);
            imgPlaceholder.setPreferredSize(new Dimension(32, 32));
            imgPlaceholder.setOpaque(true);
            imgPlaceholder.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (isEditing) showImagePopup(BankRow.this);
                }
            });
            updateRowImage(path);

            nameField = new JTextField(name);
            nameField.setPreferredSize(new Dimension(160, 30));
            nameField.setBorder(null); nameField.setOpaque(false);
            nameField.setEditable(false);

            col1.add(imgPlaceholder); col1.add(nameField);

            JPanel col2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, verticalGap));
            col2.setOpaque(false);
            Dimension d2 = new Dimension(COL2_WIDTH, isMainRow ? ROW_HEIGHT : ROW_HEIGHT - 10);
            col2.setPreferredSize(d2); col2.setMaximumSize(d2);
            col2.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, COLOR_GRIDLINE));

            amountField = new JTextField(amount);
            amountField.setPreferredSize(new Dimension(110, 30));
            amountField.setHorizontalAlignment(JTextField.RIGHT);
            amountField.setBorder(null); amountField.setOpaque(false);
            amountField.setEditable(false);
            amountField.setFont(FONT_BOLD);
            col2.add(amountField);

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
                addSubBtn.addActionListener(e -> addSubBank("New Sub", "0.00", BankRow.this.imagePath));
                col3.add(addSubBtn);
            }

            headerPanel.add(col1); headerPanel.add(col2); headerPanel.add(col3);
            add(headerPanel, BorderLayout.NORTH);

            if (isMainRow) {
                childrenPanel = new JPanel();
                childrenPanel.setLayout(new BoxLayout(childrenPanel, BoxLayout.Y_AXIS));
                childrenPanel.setBackground(COLOR_BG);
                childrenPanel.setVisible(false);
                add(childrenPanel, BorderLayout.CENTER);
            }

            headerPanel.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    selectRow(BankRow.this);
                    if (isMainRow && !isEditing) {
                        toggleExpand();
                    }
                }
            });
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        public void setSelection(boolean selected) {
            headerPanel.setBackground(selected ? COLOR_SELECTED : COLOR_BG);
            if (isMainRow) toggleBtn.setBackground(selected ? COLOR_SELECTED : COLOR_BG);
        }

        private void toggleExpand() {
            if (!isMainRow) return;
            isExpanded = !isExpanded;
            childrenPanel.setVisible(isExpanded);
            toggleBtn.setText(isExpanded ? "▼" : "▶");

            listContainer.revalidate();
            listContainer.repaint();
        }

        public void addSubBank(String name, String amount, String path) {
            BankRow subRow = new BankRow(name, amount, path, false);
            childrenPanel.add(subRow);
            bankRows.add(subRow);

            if (!isExpanded) toggleExpand();

            calculateTotal();
            listContainer.revalidate();
            listContainer.repaint();
        }

        private void toggleEditMode() {
            if (!isEditing) {
                isEditing = true;
                nameField.setEditable(true); amountField.setEditable(true);
                nameField.setOpaque(true); amountField.setOpaque(true);
                nameField.setBorder(BorderFactory.createLineBorder(COLOR_GRIDLINE));
                amountField.setBorder(BorderFactory.createLineBorder(COLOR_GRIDLINE));
                imgPlaceholder.setCursor(new Cursor(Cursor.HAND_CURSOR));
                editBtn.setText("Save"); editBtn.setBackground(new Color(0, 123, 255)); editBtn.setForeground(Color.WHITE);
            } else {
                isEditing = false;
                nameField.setEditable(false); amountField.setEditable(false);
                nameField.setOpaque(false); amountField.setOpaque(false);
                nameField.setBorder(null); amountField.setBorder(null);
                imgPlaceholder.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                editBtn.setText("Edit"); editBtn.setBackground(new Color(233, 236, 239)); editBtn.setForeground(COLOR_TEXT_MAIN);
                calculateTotal();
            }
        }

        public void updateRowImage(String path) {
            this.imagePath = path;
            ImageIcon icon = scaleIcon(path, 32, 32);
            if (icon != null) { imgPlaceholder.setIcon(icon); imgPlaceholder.setText(""); }
            else { imgPlaceholder.setIcon(null); imgPlaceholder.setText("img"); }

            if (isMainRow && childrenPanel != null) {
                for (Component child : childrenPanel.getComponents()) {
                    if (child instanceof BankRow) {
                        ((BankRow) child).updateRowImage(path);
                    }
                }
            }
        }
    }
}
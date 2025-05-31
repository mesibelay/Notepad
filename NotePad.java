
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class NotePad extends JFrame implements ActionListener {

    JTabbedPane tabbedPane;
    JMenuBar menuBar;
    JMenu fileMenu, editMenu, formatMenu, viewMenu, searchMenu;
    JMenuItem newItem, openItem, saveItem, exitItem;
    JMenuItem cutItem, copyItem, pasteItem, selectAllItem;
    JMenuItem fontItem, darkModeItem, findReplaceItem, undoItem, redoItem;
    boolean darkMode = false;

    public NotePad() {
        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
        createNewTab();

        // Menu bar
        menuBar = new JMenuBar();

        // File Menu
        fileMenu = new JMenu("File");
        newItem = new JMenuItem("New Tab");
        openItem = new JMenuItem("Open");
        saveItem = new JMenuItem("Save");
        exitItem = new JMenuItem("Exit");
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Edit Menu
        editMenu = new JMenu("Edit");
        cutItem = new JMenuItem("Cut");
        copyItem = new JMenuItem("Copy");
        pasteItem = new JMenuItem("Paste");
        selectAllItem = new JMenuItem("Select All");
        undoItem = new JMenuItem("Undo");
        redoItem = new JMenuItem("Redo");
        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        editMenu.addSeparator();
        editMenu.add(selectAllItem);
        editMenu.addSeparator();
        editMenu.add(undoItem);
        editMenu.add(redoItem);

        // Format Menu
        formatMenu = new JMenu("Format");
        fontItem = new JMenuItem("Font");
        formatMenu.add(fontItem);

        // View Menu
        viewMenu = new JMenu("View");
        darkModeItem = new JMenuItem("Toggle Dark Mode");
        viewMenu.add(darkModeItem);

        // Search Menu
        searchMenu = new JMenu("Search");
        findReplaceItem = new JMenuItem("Find & Replace");
        searchMenu.add(findReplaceItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(formatMenu);
        menuBar.add(viewMenu);
        menuBar.add(searchMenu);
        setJMenuBar(menuBar);

        for (Component c : new Component[]{newItem, openItem, saveItem, exitItem,
                cutItem, copyItem, pasteItem, selectAllItem,
                undoItem, redoItem, fontItem, darkModeItem, findReplaceItem}) {
            ((JMenuItem) c).addActionListener(this);
        }

        setTitle("Simple Notepad - Tabs & Line Numbers");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    void createNewTab() {
        JTextArea textArea = new JTextArea();
        UndoManager undoManager = new UndoManager();
        textArea.getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));
        JScrollPane scrollPane = new JScrollPane(textArea);

        TextLineNumber tln = new TextLineNumber(textArea);
        scrollPane.setRowHeaderView(tln);

        tabbedPane.addTab("Untitled", scrollPane);
        tabbedPane.setSelectedComponent(scrollPane);

        scrollPane.putClientProperty("undo", undoManager);
        scrollPane.putClientProperty("textArea", textArea);
    }

    JTextArea getCurrentTextArea() {
        JScrollPane scrollPane = (JScrollPane) tabbedPane.getSelectedComponent();
        return (JTextArea) scrollPane.getClientProperty("textArea");
    }

    UndoManager getCurrentUndoManager() {
        JScrollPane scrollPane = (JScrollPane) tabbedPane.getSelectedComponent();
        return (UndoManager) scrollPane.getClientProperty("undo");
    }
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        JTextArea textArea = getCurrentTextArea();
        UndoManager undoManager = getCurrentUndoManager();

        if (src == newItem) {
            createNewTab();
        } else if (src == openItem) {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    textArea.read(br, null);
                    tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), file.getName());
                } catch (IOException ex) {
                    showError("Error opening file.");
                }
            }
        } else if (src == saveItem) {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                    textArea.write(bw);
                    tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), file.getName());
                } catch (IOException ex) {
                    showError("Error saving file.");
                }
            }
        } else if (src == exitItem) {
            System.exit(0);
        } else if (src == cutItem) {
            textArea.cut();
        } else if (src == copyItem) {
            textArea.copy();
        } else if (src == pasteItem) {
            textArea.paste();
        } else if (src == selectAllItem) {
            textArea.selectAll();
        } else if (src == undoItem && undoManager.canUndo()) {
            undoManager.undo();
        } else if (src == redoItem && undoManager.canRedo()) {
            undoManager.redo();
        } else if (src == fontItem) {
            showFontDialog(textArea);
        } else if (src == darkModeItem) {
            toggleDarkMode(textArea);
        } else if (src == findReplaceItem) {
            showFindReplaceDialog(textArea);
        }
    }

    private void showFontDialog(JTextArea textArea) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fonts = ge.getAvailableFontFamilyNames();
        JComboBox<String> fontList = new JComboBox<>(fonts);
        JTextField sizeField = new JTextField("14", 4);
        JCheckBox bold = new JCheckBox("Bold");
        JCheckBox italic = new JCheckBox("Italic");

        JPanel panel = new JPanel();
        panel.add(new JLabel("Font:"));
        panel.add(fontList);
        panel.add(new JLabel("Size:"));
        panel.add(sizeField);
        panel.add(bold);
        panel.add(italic);

        int option = JOptionPane.showConfirmDialog(this, panel, "Choose Font", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String fontName = (String) fontList.getSelectedItem();
            int fontSize = Integer.parseInt(sizeField.getText());
            int style = (bold.isSelected() ? Font.BOLD : 0) + (italic.isSelected() ? Font.ITALIC : 0);
            textArea.setFont(new Font(fontName, style, fontSize));
        }
    }

    void toggleDarkMode(JTextArea textArea) {
        if (!darkMode) {
            textArea.setBackground(Color.DARK_GRAY);
            textArea.setForeground(Color.WHITE);
        } else {
            textArea.setBackground(Color.WHITE);
            textArea.setForeground(Color.BLACK);
        }
        darkMode = !darkMode;
    }

    private void showFindReplaceDialog(JTextArea textArea) {
        JTextField findField = new JTextField(10);
        JTextField replaceField = new JTextField(10);
        JPanel panel = new JPanel();
        panel.add(new JLabel("Find:"));
        panel.add(findField);
        panel.add(new JLabel("Replace:"));
        panel.add(replaceField);


        int option = JOptionPane.showConfirmDialog(this, panel, "Find & Replace", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String findText = findField.getText();
            String replaceText = replaceField.getText();
            textArea.setText(textArea.getText().replace(findText, replaceText));
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(NotePad::new);
    }

    // ----------------------------------
    // Inner class: TextLineNumber
    // ----------------------------------
    class TextLineNumber extends JPanel {
        private final JTextArea textArea;

        public TextLineNumber(JTextArea textArea) {
            this.textArea = textArea;
            setPreferredSize(new Dimension(40, Integer.MAX_VALUE));
            setBackground(Color.LIGHT_GRAY);
            setForeground(Color.BLACK);
            textArea.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { repaint(); }
                public void removeUpdate(DocumentEvent e) { repaint(); }
                public void changedUpdate(DocumentEvent e) { repaint(); }
            });
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            FontMetrics fm = getFontMetrics(textArea.getFont());
            int lineHeight = fm.getHeight();
            int startOffset = textArea.viewToModel2D(new Point(0, 0));
            Element root = textArea.getDocument().getDefaultRootElement();
            int startLine = root.getElementIndex(startOffset);
            int y = 0;

            for (int i = startLine; i < root.getElementCount(); i++) {
                String lineNumber = String.valueOf(i + 1);
                y = (i - startLine + 1) * lineHeight;
                g.drawString(lineNumber, 5, y - 4);
            }
        }
    }
}
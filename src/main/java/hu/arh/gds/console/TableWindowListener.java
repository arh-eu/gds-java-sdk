package hu.arh.gds.console;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.WindowListener;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import hu.arh.gds.message.data.QueryResponseHolder;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Deprecated
public class TableWindowListener implements WindowListener {
    private final WindowBasedTextGUI gui;
    private final Table<String> table;

    private final QueryResponseHolder queryResponseHolder;
    private String messageId;
    private Label fieldType;
    private Label mimeType;

    private AtomicInteger actualRow;
    private AtomicInteger actualColumn;

    private String text;
    private String actualText;

    private int counter;
    private int visibleRows;

    public TableWindowListener(WindowBasedTextGUI gui,
                               Table<String> table,
                               QueryResponseHolder queryResponseHolder,
                               String messageId,
                               Label fieldType,
                               Label mimeType,
                               int counter) {
        this.gui = gui;
        this.table = table;
        this.queryResponseHolder = queryResponseHolder;
        this.messageId = messageId;
        this.fieldType = fieldType;
        this.mimeType = mimeType;
        this.counter = counter;

        actualRow = new AtomicInteger(table.getSelectedRow());
        actualColumn = new AtomicInteger(table.getSelectedColumn());

        table.setSelectAction(() -> {
            actualRow.set(table.getSelectedRow());
            actualColumn.set(table.getSelectedColumn());
        });

        visibleRows = table.getSize().getRows();
    }

    @Override
    public void onResized(Window window, TerminalSize terminalSize, TerminalSize terminalSize1) {
//        visibleRows = (Math.max(terminalSize1.getRows() - 16, 14));
        table.setPreferredSize(terminalSize1);
        visibleRows = terminalSize1.getRows();
        updateView();
    }

    @Override
    public void onMoved(Window window, TerminalPosition terminalPosition, TerminalPosition terminalPosition1) {
    }

    @Override
    public void onInput(Window window, KeyStroke keyStroke, AtomicBoolean atomicBoolean) {
        KeyType keyType = keyStroke.getKeyType();
        switch (keyType) {
            case ArrowRight:
                onArrowRight();
                break;
            case ArrowLeft:
                onArrowLeft();
                break;
            case ArrowUp:
                onArrowUp();
                break;
            case ArrowDown:
                onArrowDown();
                break;
//            case PageUp:
//                onPageUp();
//                break;
//            case PageDown:
//                onPageDown();
//                break;
        }
        if (keyStroke.isCtrlDown()) {
            if (keyStroke.getCharacter().equals('f')) {
                search();
            } else if (keyStroke.getCharacter().equals('e')) {
                export();
            } else if (keyStroke.getCharacter().equals('g')) {
                gotoRecord();
            }
        }
    }

    @Override
    public void onUnhandledInput(Window window, KeyStroke keyStroke, AtomicBoolean atomicBoolean) {
        if (keyStroke.getKeyType().equals(KeyType.Escape)) {
            exit();
        }
    }

    private void onArrowRight() {
        if (actualColumn.get() < table.getTableModel().getColumnCount() - 1) {
            actualColumn.incrementAndGet();
        }
        updateFieldDetails();
    }

    private void onArrowLeft() {
        if (actualColumn.get() > 0) {
            actualColumn.decrementAndGet();
        }
        updateFieldDetails();
    }

    private void onArrowUp() {
        if (actualRow.get() > 0) {
            actualRow.decrementAndGet();
        }
        updateFieldDetails();
    }

    private void onArrowDown() {
        if (actualRow.get() < table.getTableModel().getRowCount() - 1) {
            actualRow.incrementAndGet();
        }
        updateFieldDetails();
    }


    private void onPageUp() {
        actualRow.set(Math.max(0, actualRow.get() - visibleRows));
        updateFieldDetails();
    }

    private void onPageDown() {
        actualRow.set(Math.min(table.getTableModel().getRowCount() - 1, actualRow.get() + visibleRows));
        updateFieldDetails();
    }


    private int clampBetween(int value, int min, int max) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        }
        return value;
    }

    private void updateView() {
        System.err.println("Actual: " + actualRow.get());
        System.err.println("ViewTop: " + table.getViewTopRow());
        System.err.println("Visible: " + visibleRows);
        if (actualRow.get() - table.getViewTopRow() > visibleRows) {
            table.setViewTopRow(clampBetween(table.getViewTopRow() + 1, 0, table.getTableModel().getRowCount() - 1));
        } else if (actualRow.get() < table.getViewTopRow()) {
            table.setViewTopRow(clampBetween(table.getViewTopRow() - 1, 0, table.getTableModel().getRowCount() - 1));
        }
    }

    private void updateFieldDetails() {
        updateView();
        if (actualColumn.get() != 0) {
            fieldType.setText("Field type: " + queryResponseHolder.getFieldHolders().get(actualColumn.get() - 1).getFieldType());
            mimeType.setText("Mime type: " + queryResponseHolder.getFieldHolders().get(actualColumn.get() - 1).getMimeType());
        } else {
            fieldType.setText("Field type: -");
            mimeType.setText("Mime type: -");
        }
    }

    private void gotoRecord() {
        String rowString = TextInputDialog.showDialog(gui, "Goto record", null, "");
        if (rowString != null) {
            if (!rowString.equals("")) {
                int row;
                try {
                    row = Integer.parseInt(rowString);
                } catch (NumberFormatException e) {
                    return;
                }
                if (row >= 0 && row <= table.getTableModel().getRowCount()) {
                    table.setSelectedRow(row - 1);
                    actualRow.set(row - 1);
                    actualColumn.set(1);
                    table.setSelectedColumn(1);
                    updateFieldDetails();
                    try {
                        gui.updateScreen();
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }

    private void exit() {
        gui.getActiveWindow().close();
        //if(true) {
        if (!queryResponseHolder.getMorePage()) {
            try {
                gui.getScreen().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void export() {
        try {
            String filePath = Utils.exportTableToCsv(
                    messageId,
                    counter,
                    table.getTableModel().getColumnLabels(),
                    table.getTableModel().getRows());
            MessageDialog.showMessageDialog(gui, "Export", "Exported to " + filePath);
        } catch (IOException e) {
            MessageDialog.showMessageDialog(gui, "Export", "An error occurred while exporting: " + e.getMessage());
        }
    }

    private void search() {
        text = TextInputDialog.showDialog(gui, "Search", null, text == null ? "" : text);
        if (text != null) {
            TableModel<String> tableModel = table.getTableModel();
            List<List<String>> rows = tableModel.getRows();
            for (int i = 0; i < rows.size(); ++i) {
                for (int j = 1; j < rows.get(i).size(); ++j) {
                    if (rows.get(i).get(j).equals(text)) {
                        if (actualText != null && actualText.equals(text)) {
                            if (i < actualRow.get() || (i == actualRow.get() && j <= actualColumn.get())) {
                                continue;
                            }
                        }
                        table.setSelectedRow(i);
                        table.setSelectedColumn(j);
                        actualRow.set(i);
                        actualColumn.set(j);
                        try {
                            gui.updateScreen();
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                        actualText = text;
                        return;
                    }
                }
            }
        }
    }
}

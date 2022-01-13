/*
 * Intellectual property of ARH Inc.
 * This file belongs to the GDS 5.1 system in the gds-messages project.
 * Budapest, 2020/10/14
 */

package hu.arheu.gds.console;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.screen.VirtualScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.swing.*;
import hu.arheu.gds.message.data.FieldValueType;
import hu.arheu.gds.message.data.QueryResponseHolder;
import org.msgpack.value.Value;

import javax.swing.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConsoleGUI {
    private final int pageCount;
    private final String messageID;
    private final QueryResponseHolder responseHolder;

    private Table<String> dataPanel;
    private Label fieldType;
    private Label mimeType;

    private int selectedColumn = 0;
    private int selectedRow = 0;

    private String previousSearch;
    private boolean lastChecked;

    private Screen screen;
    private WindowBasedTextGUI textGUI;

    public ConsoleGUI(int pageCount, String messageID, QueryResponseHolder responseHolder) {
        this.pageCount = pageCount;
        this.messageID = messageID;
        this.responseHolder = responseHolder;
    }

    public void display() throws IOException {
        try (Screen createdScreen = createScreen()) {
            this.screen = new VirtualScreen(createdScreen);
            screen.startScreen();
            textGUI = new MultiWindowTextGUI(screen);

            Window window = new BasicWindow("GDS Query result - page " + pageCount);
            Panel content = new Panel(new LinearLayout(Direction.VERTICAL));

            content.addComponent(createHeaderPanel());
            content.addComponent(createFieldDetailsPanel());
            dataPanel = createDataPanel();
            dataPanel.setVisibleRows(15);

            content.addComponent(dataPanel);
            content.addComponent(createHotKeysPanel());

            window.setComponent(content);

            textGUI.setTheme(new TableWindowThemeDefinition(screen));

            window.addWindowListener(createListener(textGUI));
            textGUI.addWindowAndWait(window);
        }
    }

    private WindowListener createListener(final WindowBasedTextGUI textGUI) {
        return new WindowListener() {

            @Override
            public void onResized(Window window, TerminalSize oldSize, TerminalSize newSize) {
            }

            @Override
            public void onMoved(Window window, TerminalPosition oldPosition, TerminalPosition newPosition) {
            }

            @Override
            public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {

                KeyType keyType = keyStroke.getKeyType();
                if (dataPanel.getTableModel().getRowCount() == 0) {
                    if (keyType == KeyType.End || keyType == KeyType.Home) {
                        deliverEvent.set(false);
                        return;
                    }
                }

                if (keyType == KeyType.ArrowLeft && selectedColumn > 0) {
                    --selectedColumn;
                }
                if (keyType == KeyType.ArrowRight && selectedColumn < dataPanel.getTableModel().getColumnCount() - 1) {
                    ++selectedColumn;
                }


                if (keyType == KeyType.ArrowUp && selectedRow > 0) {
                    --selectedRow;
                }
                if (keyType == KeyType.ArrowDown && selectedColumn < dataPanel.getTableModel().getRowCount() - 1) {
                    ++selectedRow;
                }

                mimeAndScreenRefresh();
            }

            @Override
            public void onUnhandledInput(Window basePane, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {
                if ((keyStroke.getKeyType() == KeyType.Escape || keyStroke.getKeyType() == KeyType.EOF)) {
                    try {
                        textGUI.getScreen().close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (keyStroke.isCtrlDown()) {
                    Character pressedChar = keyStroke.getCharacter();
                    if(pressedChar != null) {
                        if (pressedChar.equals('s')) {
                            exportTableToCSV();
                        } else if (pressedChar.equals('f')) {
                            searchRecordValue();
                        } else if (pressedChar.equals('g')) {
                            goToLine();
                        }
                    }
                }
            }
        };
    }

    private void mimeAndScreenRefresh() {
        if (selectedColumn == 0) {
            fieldType.setText("Field type: -");
            mimeType.setText("Mime type: -");
        } else {
            fieldType.setText("Field type: " + responseHolder.getFieldHolders().get(selectedColumn - 1).getFieldType());
            mimeType.setText("Mime type: " + responseHolder.getFieldHolders().get(selectedColumn - 1).getMimeType());
        }
        try {
            screen.refresh();
        } catch (IOException ignored) {

        }
    }

    private void exportTableToCSV() {
        try {
            String filePath = Utils.exportTableToCsv(
                    messageID,
                    pageCount,
                    dataPanel.getTableModel().getColumnLabels(),
                    dataPanel.getTableModel().getRows());
            MessageDialog.showMessageDialog(textGUI, "Export", "Exported to " + filePath);
        } catch (IOException e) {
            MessageDialog.showMessageDialog(textGUI, "Export", "An error occurred while exporting: " + e.getMessage());
        }
    }

    private void goToLine() {
        String rowString = TextInputDialog.showDialog(textGUI, "Go to line", null, "");
        if (rowString != null && !rowString.trim().isEmpty()) {
            int row;
            try {
                row = Integer.parseInt(rowString);
            } catch (NumberFormatException e) {
                return;
            }
            if (row > 0 && row <= dataPanel.getTableModel().getRowCount()) {
                selectedRow = row - 1;
                dataPanel.setSelectedRow(selectedRow);
                mimeAndScreenRefresh();
            }
        }
    }

    private void searchRecordValue() {
        SearchDialog searchDialog = new SearchDialog("Search for record..", previousSearch, lastChecked);
        String text = searchDialog.showDialog(textGUI);

        if (text != null) {
            previousSearch = text;
            lastChecked = searchDialog.isBoxChecked();
            TableModel<String> tableModel = dataPanel.getTableModel();
            List<List<String>> rows = tableModel.getRows();
            for (int i = selectedRow; i < rows.size(); ++i) {
                for (int j = selectedColumn; j < rows.get(i).size(); ++j) {

                    boolean match;

                    if (searchDialog.isBoxChecked()) {
                        match = rows.get(i).get(j).equalsIgnoreCase(previousSearch);
                        //prevMatch = previousSearch.equalsIgnoreCase(previousSearch);
                    } else {
                        //prevMatch = Objects.equals(previousSearch, previousSearch);
                        match = rows.get(i).get(j).equals(previousSearch);
                    }

                    if (match) {
                        //if we are searching for the same value again, we have to go to the next result
                        if (i < selectedRow || (i == selectedRow && j <= selectedColumn)) {
                            continue;
                        }

                        selectedRow = i;
                        selectedColumn = j;

                        dataPanel.setSelectedRow(selectedRow);
                        dataPanel.setSelectedColumn(selectedColumn);

                        mimeAndScreenRefresh();
                        return;

                    }
                }
            }

            MessageDialog.showMessageDialog(textGUI, "Search", "Could not find more result for \"" + previousSearch + "\"");
        }
    }

    private Screen createScreen() throws IOException {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            SwingTerminalFrame swingTerminalFrame = new SwingTerminalFrame(
                    "GDS Console Client - Query Result Page #" + pageCount,
                    TerminalEmulatorDeviceConfiguration.getDefault(),
                    SwingTerminalFontConfiguration.getDefault(),
                    TerminalEmulatorColorConfiguration.getDefault(),
                    TerminalEmulatorAutoCloseTrigger.CloseOnExitPrivateMode);
            swingTerminalFrame.setDefaultCloseOperation(
                    WindowConstants.EXIT_ON_CLOSE);
            swingTerminalFrame.setVisible(true);
            return new TerminalScreen(swingTerminalFrame);
        } else {
            return new DefaultTerminalFactory().createScreen();
        }
    }

    private Panel createHeaderPanel() {
        Panel panel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        panel.addComponent(new Label("Hits: " + responseHolder.getNumberOfHits()).withBorder(Borders.singleLine()));
        panel.addComponent(new Label("Filtered hits: " + responseHolder.getNumberOfFilteredHits()).withBorder(Borders.singleLine()));
        panel.addComponent(new Label("More page: " + responseHolder.getMorePage()).withBorder(Borders.singleLine()));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(responseHolder.getQueryContextHolder().getQueryStartTime());
        panel.addComponent(new Label("Query start time: " + sdf.format(date)).withBorder(Borders.singleLine()));
        panel.addComponent(new Label("Query: " + responseHolder.getQueryContextHolder().getQuery()).withBorder(Borders.singleLine()));
        panel.addComponent(new Label("Message ID: " + messageID).withBorder(Borders.singleLine()));
        return panel;
    }

    private Panel createFieldDetailsPanel() {
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        fieldType = new Label("Field type: -");
        panel.addComponent(fieldType);
        mimeType = new Label("Mime type: -");
        panel.addComponent(mimeType);
        return panel;
    }

    private Panel createHotKeysPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        panel.addComponent(new Label("ESC Exit").withBorder(Borders.singleLine()));
        panel.addComponent(new Label("^S Export to CSV").withBorder(Borders.singleLine()));
        panel.addComponent(new Label("^F Search ").withBorder(Borders.singleLine()));
        panel.addComponent(new Label("^G Go to line").withBorder(Borders.singleLine()));
        return panel;
    }

    private Table<String> createDataPanel() {
        String[] headers = new String[responseHolder.getFieldHolders().size() + 1];
        headers[0] = "";
        for (int i = 1; i < headers.length; ++i) {
            headers[i] = responseHolder.getFieldHolders().get(i - 1).getFieldName();
        }
        Table<String> table = new Table<>(headers);
        int c = 1;
        for (List<Value> hit : responseHolder.getHits()) {
            List<String> row = new ArrayList<>();
            row.add(String.valueOf(c++));
            int col = 0;
            for (Value value : hit) {
                FieldValueType fieldType = responseHolder.getFieldHolders().get(col).getFieldType();
                if (fieldType == FieldValueType.BINARY || fieldType == FieldValueType.BINARY_ARRAY) {
                    row.add("<binary data>");
                } else {
                    row.add(value.toString());
                }
                ++col;
            }
            table.getTableModel().addRow(row);
        }
        table.setCellSelection(true);
        return table;
    }

    private static class SearchDialog extends DialogWindow {
        private String result;
        private final CheckBox caseInsensitiveBox;

        SearchDialog(String title, String value, boolean boxChecked) {
            super(title);
            setHints(Collections.singleton(Hint.CENTERED));

            TextBox textBox = new TextBox();
            if (value != null) {
                textBox.setText(value);
            }
            Panel buttonPanel = new Panel();
            buttonPanel.setLayoutManager(new GridLayout(2).setHorizontalSpacing(1));
            buttonPanel.addComponent(new Button(LocalizedString.OK.toString(), () -> {
                result = textBox.getText();
                close();
            }).setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER, true, false)));

            buttonPanel.addComponent(new Button(LocalizedString.Cancel.toString(), this::close));

            Panel mainPanel = new Panel();
            mainPanel.setLayoutManager(
                    new GridLayout(1)
                            .setLeftMarginSize(1)
                            .setRightMarginSize(1));

            mainPanel.addComponent(new EmptySpace(TerminalSize.ONE));
            textBox.setLayoutData(
                            GridLayout.createLayoutData(
                                    GridLayout.Alignment.FILL,
                                    GridLayout.Alignment.CENTER,
                                    true,
                                    false))
                    .addTo(mainPanel);
            mainPanel.addComponent(new EmptySpace(TerminalSize.ONE));


            caseInsensitiveBox = new CheckBox("Case insensitive search");
            caseInsensitiveBox.setChecked(boxChecked);
            caseInsensitiveBox.setLayoutData(
                            GridLayout.createLayoutData(
                                    GridLayout.Alignment.FILL,
                                    GridLayout.Alignment.CENTER,
                                    true,
                                    false))
                    .addTo(mainPanel);

            mainPanel.addComponent(new EmptySpace(TerminalSize.ONE));

            buttonPanel.setLayoutData(
                            GridLayout.createLayoutData(
                                    GridLayout.Alignment.END,
                                    GridLayout.Alignment.CENTER,
                                    false,
                                    false))
                    .addTo(mainPanel);
            setComponent(mainPanel);
        }

        boolean isBoxChecked() {
            return caseInsensitiveBox.isChecked();
        }

        @Override
        public String showDialog(WindowBasedTextGUI textGUI) {
            result = null;
            super.showDialog(textGUI);
            return result;
        }
    }
}

/*
 * Intellectual property of ARH Inc.
 * This file belongs to the GDS 5.1 system in the gds-messages project.
 * Budapest, 2020/10/14
 */

package hu.arh.gds.console;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.swing.*;
import hu.arh.gds.message.data.QueryResponseHolder;
import org.msgpack.value.Value;

import javax.swing.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    public ConsoleGUI(int pageCount, String messageID, QueryResponseHolder responseHolder) {
        this.pageCount = pageCount;
        this.messageID = messageID;
        this.responseHolder = responseHolder;
    }

    public void display() throws IOException {
        try (Screen screen = createScreen()) {
            screen.startScreen();
            final WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);

            Window window = new BasicWindow("GDS Query result - page " + pageCount);
            Panel content = new Panel(new LinearLayout(Direction.VERTICAL));

            content.addComponent(getHeaderPanel());
            //content.addComponent(getHotKeysPanel()); //TODO
            content.addComponent(getFieldDetailsPanel());
            dataPanel = getDataPanel();
            dataPanel.setVisibleRows(12);

            content.addComponent(dataPanel);

            window.setComponent(content);

            textGUI.setTheme(new TableWindowThemeDefinition(screen));

            window.addWindowListener(createListener(textGUI, screen));
            textGUI.addWindowAndWait(window);
        }
    }

    private WindowListener createListener(final WindowBasedTextGUI textGUI, final Screen screen) {
        return new WindowListener() {
            private int selectedColumn = 0;
            private int selectedRow = 0;

            @Override
            public void onResized(Window window, TerminalSize oldSize, TerminalSize newSize) {
            }

            @Override
            public void onMoved(Window window, TerminalPosition oldPosition, TerminalPosition newPosition) {
            }

            @Override
            public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
                KeyType keyType = keyStroke.getKeyType();
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

                if (selectedColumn == 0) {
                    fieldType.setText("Field type: -");
                    mimeType.setText("Mime type: -");
                } else {
                    fieldType.setText("Field type: " + responseHolder.getFieldHolders().get(selectedColumn - 1).getFieldType());
                    mimeType.setText("Mime type: " + responseHolder.getFieldHolders().get(selectedColumn - 1).getMimeType());
                }
                try {
                    screen.refresh();
                } catch (IOException e) {

                }
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
            }
        };
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

    private Panel getHeaderPanel() {
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

    private Panel getFieldDetailsPanel() {
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        fieldType = new Label("Field type: -");
        panel.addComponent(fieldType);
        mimeType = new Label("Mime type: -");
        panel.addComponent(mimeType);
        return panel;
    }

    private Panel getHotKeysPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        panel.addComponent(new Label("ESC exit").withBorder(Borders.singleLine()));
        panel.addComponent(new Label("^F search").withBorder(Borders.singleLine()));
        panel.addComponent(new Label("^G goto record").withBorder(Borders.singleLine()));
        panel.addComponent(new Label("^E export to CSV").withBorder(Borders.singleLine()));
        return panel;
    }

    private Table<String> getDataPanel() {
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
            for (Value value : hit) {
                row.add(value.toString());
            }
            table.getTableModel().addRow(row);
        }
        table.setCellSelection(true);
        return table;
    }
}

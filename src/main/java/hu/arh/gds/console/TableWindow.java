package hu.arh.gds.console;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.table.*;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TableWindow extends BasicWindow {
    private final QueryResponseHolder queryResponseHolder;
    private final String messageId;

    private final Screen screen;
    private final MultiWindowTextGUI textGUI;

    private final Table<String> table;
    private Label fieldTypeLabel;
    private Label mimeTypeLabel;

    private int counter;

    public TableWindow(MultiWindowTextGUI gui, Screen screen, QueryResponseHolder queryResponseHolder,
                       String messageId,
                       int counter) throws IOException {

        this.queryResponseHolder = queryResponseHolder;
        this.messageId = messageId;
        this.counter = counter;

        this.textGUI = gui;
        this.screen = screen;

        /*
        if (Utils.isWindows()) {
            SwingTerminalFrame swingTerminalFrame = new SwingTerminalFrame(
                    "gds console client",
                    TerminalEmulatorDeviceConfiguration.getDefault(),
                    SwingTerminalFontConfiguration.getDefault(),
                    TerminalEmulatorColorConfiguration.getDefault(),
                    TerminalEmulatorAutoCloseTrigger.CloseOnExitPrivateMode);
            swingTerminalFrame.setDefaultCloseOperation(
                    WindowConstants.EXIT_ON_CLOSE);
            swingTerminalFrame.setVisible(true);
            screen = new TerminalScreen(swingTerminalFrame);
        } else {
            screen = new DefaultTerminalFactory().createScreen();
        }

        textGUI = new MultiWindowTextGUI(screen);
        textGUI.setTheme(new TableWindowThemeDefinition(screen));
        */

        this.table = getTable();

        Panel panel = new Panel();
        panel.addComponent(getHeaderPanel());
        panel.addComponent(getFieldDetailsPanel());
        panel.addComponent(table);
        panel.addComponent(getFooterPanel());

        table.setPreferredSize(new TerminalSize(screen.getTerminalSize().getColumns(), queryResponseHolder.getHits().size() + 2));


        this.setComponent(panel);
        this.setHints(Arrays.asList(Hint.CENTERED));
    }

    private Panel getHeaderPanel() {
        Panel panel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        panel.addComponent(new Label("hits: " + queryResponseHolder.getNumberOfHits()).withBorder(Borders.singleLine()));
        panel.addComponent(new Label("filtered hits: " + queryResponseHolder.getNumberOfFilteredHits()).withBorder(Borders.singleLine()));
        panel.addComponent(new Label("more page: " + queryResponseHolder.getMorePage()).withBorder(Borders.singleLine()));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(queryResponseHolder.getQueryContextHolder().getQueryStartTime());
        panel.addComponent(new Label("query start time: " + sdf.format(date)).withBorder(Borders.singleLine()));
        panel.addComponent(new Label("query: " + queryResponseHolder.getQueryContextHolder().getQuery()).withBorder(Borders.singleLine()));
        return panel;
    }

    private Panel getFieldDetailsPanel() {
        Panel panel = new Panel();
        this.fieldTypeLabel = new Label("Field type: -");
        this.mimeTypeLabel = new Label("Mime type: -");
        panel.addComponent(fieldTypeLabel);
        panel.addComponent(mimeTypeLabel);
        return panel;
    }

    private Panel getFooterPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        panel.addComponent(new Label("ESC exit").withBorder(Borders.singleLine()));
        panel.addComponent(new Label("^F search").withBorder(Borders.singleLine()));
        panel.addComponent(new Label("^G goto record").withBorder(Borders.singleLine()));
        panel.addComponent(new Label("^E export to CSV").withBorder(Borders.singleLine()));
        return panel;
    }

    private Table<String> getTable() {
        String[] headers = new String[queryResponseHolder.getfFieldHolders().size() + 1];
        headers[0] = "";
        for (int i = 1; i < headers.length; ++i) {
            headers[i] = queryResponseHolder.getfFieldHolders().get(i - 1).getFieldName();
        }
        Table<String> table = new Table<>(headers);
        int c = 1;
        for (List<Value> hit : queryResponseHolder.getHits()) {
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

    public void show() throws IOException {
        screen.startScreen();
        this.addWindowListener(new TableWindowListener(
                textGUI,
                table,
                queryResponseHolder,
                messageId,
                fieldTypeLabel,
                mimeTypeLabel,
                counter));
        textGUI.addWindow(this);
        textGUI.waitForWindowToClose(this);
    }
}

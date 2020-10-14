/*
 * Intellectual property of ARH Inc.
 * This file belongs to the GDS 5.1 system in the gds-messages project.
 * Budapest, 2020/10/14
 */

package hu.arh.gds.console;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.swing.*;
import hu.arh.gds.message.data.QueryResponseHolder;

import javax.swing.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

class ConsoleGUI {
    private ConsoleGUI() {
    }

    static void display(int pageCount, String messageID, QueryResponseHolder responseHolder) throws IOException {
        try (Screen screen = setupScreen()) {
            screen.startScreen();
            final WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);

            Window window = new BasicWindow("GDS Query result - page " + pageCount);
            Panel content = new Panel(new LinearLayout(Direction.HORIZONTAL));

            content.addComponent(getHeaderPanel(messageID, responseHolder));
            window.setComponent(content);

            textGUI.setTheme(new TableWindowThemeDefinition(screen));

            window.addWindowListener(new WindowListener() {
                                         @Override
                                         public void onResized(Window window, TerminalSize oldSize, TerminalSize newSize) {

                                         }

                                         @Override
                                         public void onMoved(Window window, TerminalPosition oldPosition, TerminalPosition newPosition) {

                                         }

                                         @Override
                                         public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {

                                         }

                                         @Override
                                         public void onUnhandledInput(Window basePane, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {
                                             System.err.println("UNHANDLED INPUT: " + keyStroke.getKeyType());
                                             if ((keyStroke.getKeyType() == KeyType.Escape || keyStroke.getKeyType() == KeyType.EOF)) {
                                                 try {
                                                     System.err.println("Closing screen...");
                                                     textGUI.getScreen().close();
                                                 } catch (IOException e) {
                                                     throw new RuntimeException(e);
                                                 }
                                             }
                                         }
                                     }
            );


            textGUI.addWindowAndWait(window);

        }
    }

    private static Screen setupScreen() throws IOException {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            SwingTerminalFrame swingTerminalFrame = new SwingTerminalFrame(
                    "GDS Console Client - Query Result display",
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

    private static Panel getHeaderPanel(String messageID, QueryResponseHolder queryResponseHolder) {
        Panel panel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        panel.addComponent(new Label("Hits: " + queryResponseHolder.getNumberOfHits()).withBorder(Borders.singleLine()));
        panel.addComponent(new Label("Filtered hits: " + queryResponseHolder.getNumberOfFilteredHits()).withBorder(Borders.singleLine()));
        panel.addComponent(new Label("More page: " + queryResponseHolder.getMorePage()).withBorder(Borders.singleLine()));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(queryResponseHolder.getQueryContextHolder().getQueryStartTime());
        panel.addComponent(new Label("Query start time: " + sdf.format(date)).withBorder(Borders.singleLine()));
        panel.addComponent(new Label("Query: " + queryResponseHolder.getQueryContextHolder().getQuery()).withBorder(Borders.singleLine()));
        panel.addComponent(new Label("Message ID: " + messageID).withBorder(Borders.singleLine()));
        return panel;
    }
}

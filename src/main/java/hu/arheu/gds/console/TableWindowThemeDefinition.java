package hu.arheu.gds.console;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.graphics.Theme;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.graphics.ThemeStyle;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.ComponentRenderer;
import com.googlecode.lanterna.gui2.WindowDecorationRenderer;
import com.googlecode.lanterna.gui2.WindowPostRenderer;
import com.googlecode.lanterna.screen.Screen;

import java.util.EnumSet;

public class TableWindowThemeDefinition implements Theme {
    private final TextGraphics textGraphics;
    private Theme defaultTheme = LanternaThemes.getDefaultTheme();
    private ThemeDefinition defaultThemeDefinition = defaultTheme.getDefaultDefinition();

    public TableWindowThemeDefinition(Screen screen) {
        this.textGraphics = screen.newTextGraphics();
    }

    @Override
    public ThemeDefinition getDefaultDefinition() {
        return defaultThemeDefinition;
    }

    private ThemeStyle getDefaultThemeStyle() {
        return new ThemeStyle() {
            @Override
            public TextColor getForeground() {
                return textGraphics.getForegroundColor();
            }

            @Override
            public TextColor getBackground() {
                return textGraphics.getBackgroundColor();
            }

            @Override
            public EnumSet<SGR> getSGRs() {
                return textGraphics.getActiveModifiers();
            }
        };
    }

    @Override
    public ThemeDefinition getDefinition(Class<?> aClass) {
        return new ThemeDefinition() {
            @Override
            public ThemeStyle getNormal() {
                return getDefaultThemeStyle();
            }

            @Override
            public ThemeStyle getPreLight() {
                return getDefaultThemeStyle();
            }

            @Override
            public ThemeStyle getSelected() {
                return getDefaultThemeStyle();
            }

            @Override
            public ThemeStyle getActive() {
                return new ThemeStyle() {
                    @Override
                    public TextColor getForeground() {
                        return textGraphics.getForegroundColor();
                    }

                    @Override
                    public TextColor getBackground() {
                        return TextColor.ANSI.WHITE;
                    }

                    @Override
                    public EnumSet<SGR> getSGRs() {
                        return defaultThemeDefinition.getSelected().getSGRs();
                    }
                };
            }

            @Override
            public ThemeStyle getInsensitive() {
                return getDefaultThemeStyle();
            }

            @Override
            public ThemeStyle getCustom(String s) {
                return defaultThemeDefinition.getCustom(s);
            }

            @Override
            public ThemeStyle getCustom(String s, ThemeStyle themeStyle) {
                if (s.equals("HEADER")) {
                    return new ThemeStyle() {
                        @Override
                        public TextColor getForeground() {
                            return textGraphics.getForegroundColor();
                        }

                        @Override
                        public TextColor getBackground() {
                            return textGraphics.getBackgroundColor();
                        }

                        @Override
                        public EnumSet<SGR> getSGRs() {
                            EnumSet<SGR> sgrs = textGraphics.getActiveModifiers().clone();
                            sgrs.add(SGR.BOLD);
                            return sgrs;
                        }
                    };
                }
                return defaultThemeDefinition.getCustom(s, themeStyle);
            }

            @Override
            public boolean getBooleanProperty(String s, boolean b) {
                return defaultThemeDefinition.getBooleanProperty(s, b);
            }

            @Override
            public boolean isCursorVisible() {
                return defaultThemeDefinition.isCursorVisible();
            }

            @Override
            public char getCharacter(String s, char c) {
                return defaultThemeDefinition.getCharacter(s, c);
            }

            @Override
            public <T extends Component> ComponentRenderer<T> getRenderer(Class<T> aClass) {
                return defaultThemeDefinition.getRenderer(aClass);
            }
        };
    }

    @Override
    public WindowPostRenderer getWindowPostRenderer() {
        return defaultTheme.getWindowPostRenderer();
    }

    @Override
    public WindowDecorationRenderer getWindowDecorationRenderer() {
        return defaultTheme.getWindowDecorationRenderer();
    }
}

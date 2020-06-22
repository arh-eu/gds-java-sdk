package hu.arh.gds.console.commands;

import com.beust.jcommander.IStringConverter;
import hu.arh.gds.message.util.Utils;

public class HexConverter implements IStringConverter<String> {
    @Override
    public String convert(String value) {
        return "0x" + Utils.stringToUTF8Hex(value);
    }
}
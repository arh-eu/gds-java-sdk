package hu.arheu.gds.console.parser;

import com.beust.jcommander.IStringConverter;
import hu.arheu.gds.message.util.Converters;

public class HexConverter implements IStringConverter<String> {
    @Override
    public String convert(String value) {
        return "0x" + Converters.stringToUTF8Hex(value);
    }
}

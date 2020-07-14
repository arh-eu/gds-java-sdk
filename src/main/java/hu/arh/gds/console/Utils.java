package hu.arh.gds.console;

import com.google.gson.*;
import hu.arh.gds.message.data.MessageData;
import hu.arh.gds.message.data.MessageDataTypeHelper;
import hu.arh.gds.message.header.MessageHeader;
import hu.arh.gds.message.header.MessageHeaderTypeHelper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.msgpack.value.Value;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {
    private static final String EXPORTS_FOLDER_NAME = "exports";
    private static final String ATTACHMENTS_FOLDER_NAME = "attachments";
    private static final String OS = System.getProperty("os.name").toLowerCase();

    private static final Gson gson;

    private static final Map<String, String> mimeExtensions = new HashMap<>();

    static {

        mimeExtensions.put("image/bmp", "bmp");
        mimeExtensions.put("image/png", "png");
        mimeExtensions.put("image/jpg", "jpg");
        mimeExtensions.put("image/jpeg", "jpg");

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setLenient();
        JsonSerializer<Value> valueJsonSerializer = (value, type, jsonSerializationContext) ->
                JsonParser.parseString(value.toJson());
        JsonSerializer<byte[]> binaryJsonSerializer = (value, type, jsonSerializationContext) ->
                new JsonParser().parse(value == null ? "null" : String.valueOf(value.length) + "bytes");
        gsonBuilder.registerTypeAdapter(Value.class, valueJsonSerializer);
        gsonBuilder.registerTypeAdapter(byte[].class, binaryJsonSerializer);
        ExclusionStrategy strategy = new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                if (fieldAttributes.getName().equals("cache")
                        || fieldAttributes.getName().equals("messageSize")
                        || (fieldAttributes.getName().equals("binary"))) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean shouldSkipClass(Class<?> aClass) {
                if (aClass.equals(MessageHeaderTypeHelper.class)
                        || aClass.equals(MessageDataTypeHelper.class)) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        gsonBuilder.setExclusionStrategies(strategy);
        gson = gsonBuilder.setPrettyPrinting().serializeNulls().create();
    }

    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    private static void createFolder(String name) {
        File folder = new File(name);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    private static void createExportsFolder() {
        createFolder(EXPORTS_FOLDER_NAME);
    }

    private static void createAttachmentsFolder() {
        createFolder(ATTACHMENTS_FOLDER_NAME);
    }

    public static String exportTableToCsv(String messageId, int counter, List<String> headers, List<List<String>> records) throws IOException {
        createExportsFolder();
        FileWriter out = new FileWriter(EXPORTS_FOLDER_NAME + "/" + messageId + "-" + counter + "-csv.csv");
        CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
                .withHeader(Arrays.copyOf(headers.toArray(), headers.size(), String[].class)));
        printer.printRecords(records);
        printer.close();
        return EXPORTS_FOLDER_NAME + "/" + messageId + "-" + counter + "-csv.csv";
    }

    public static String getJsonFromMessage(MessageHeader header, MessageData data) {
        return gson.toJson(new Message(header, data));
    }

    public static void exportJson(String messageId, String json) throws IOException {
        writeToFile("exports" + "/" + messageId + "-json.json", json);
    }

    private static void writeToFile(String path, String s) throws IOException {
        createExportsFolder();
        FileOutputStream outputStream = new FileOutputStream(path);
        byte[] strToBytes = s.getBytes();
        outputStream.write(strToBytes);
        outputStream.close();
    }

    public static void saveAttachment(String messageId, byte[] attachment, String meta) throws IOException {
        createAttachmentsFolder();
        String extension;
        if(mimeExtensions.containsKey(meta)) {
            extension = mimeExtensions.get(meta);
        } else {
            extension = "unknown";
        }
        OutputStream os = new FileOutputStream(ATTACHMENTS_FOLDER_NAME + "/" + messageId + "-attachment." + extension);
        os.write(attachment);
        os.close();
    }
}

package hu.arheu.gds.console;

import com.google.gson.*;
import hu.arheu.gds.message.FullGdsMessage;
import hu.arheu.gds.message.data.MessageData;
import hu.arheu.gds.message.header.MessageHeaderBase;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.msgpack.value.Value;

import java.io.*;
import java.util.*;

public class Utils {
    private static final String EXPORTS_FOLDER_NAME = "exports";
    private static final String ATTACHMENTS_FOLDER_NAME = "attachments";

    private static final Gson gson;

    private static final Map<String, String> mimeExtensions = new HashMap<>();

    private static final Set<String> skippedAttributes = new HashSet<>();
    private static final Set<Class<?>> skippedClasses = new HashSet<>();

    static {

        mimeExtensions.put("image/bmp", "bmp");
        mimeExtensions.put("image/png", "png");
        mimeExtensions.put("image/jpg", "jpg");
        mimeExtensions.put("image/jpeg", "jpg");

        skippedAttributes.add("binary");
        skippedAttributes.add("cache");
        skippedAttributes.add("messageSize");


        GsonBuilder gsonBuilder = new GsonBuilder().setLenient();
        JsonSerializer<Value> valueJsonSerializer = (value, type, jsonSerializationContext) ->
                JsonParser.parseString(value.toJson());
        JsonSerializer<byte[]> binaryJsonSerializer = (value, type, jsonSerializationContext) ->
                JsonParser.parseString(value == null ? "null" : "'" + value.length + " bytes'");

        gsonBuilder.registerTypeAdapter(Value.class, valueJsonSerializer);
        gsonBuilder.registerTypeAdapter(byte[].class, binaryJsonSerializer);

        ExclusionStrategy strategy = new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                return skippedAttributes.contains(fieldAttributes.getName());
            }

            @Override
            public boolean shouldSkipClass(Class<?> aClass) {
                return skippedClasses.contains(aClass);
            }
        };

        gson = gsonBuilder.setExclusionStrategies(strategy).setPrettyPrinting().serializeNulls().create();
    }

    private static void createFolder(String name) {
        File folder = new File(name);
        if (!folder.exists()) {
            //noinspection ResultOfMethodCallIgnored
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
                .builder().setHeader(Arrays.copyOf(headers.toArray(), headers.size(), String[].class)).build());
        printer.printRecords(records);
        printer.close();
        return EXPORTS_FOLDER_NAME + "/" + messageId + "-" + counter + "-csv.csv";
    }

    public static String getJsonFromMessage(MessageHeaderBase header, MessageData data) {
        return gson.toJson(new FullGdsMessage(header, data));
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
//        extension = mimeExtensions.getOrDefault(meta, "unknown");
        extension = "unknown";
        for (String entries : mimeExtensions.keySet()) {
            if (meta != null && meta.contains(entries)) {
                extension = mimeExtensions.get(entries);
                break;
            }
        }
        OutputStream os = new FileOutputStream(ATTACHMENTS_FOLDER_NAME + "/" + messageId + "-attachment." + extension);
        os.write(attachment);
        os.close();
    }
}

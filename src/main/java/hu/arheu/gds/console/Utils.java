package hu.arheu.gds.console;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import hu.arheu.gds.message.FullGdsMessage;
import hu.arheu.gds.message.data.MessageData;
import hu.arheu.gds.message.header.MessageHeaderBase;
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

    private static final ObjectMapper OBJECT_MAPPER;

    private static final Map<String, String> mimeExtensions = new HashMap<>();

    static {

        mimeExtensions.put("image/bmp", "bmp");
        mimeExtensions.put("image/png", "png");
        mimeExtensions.put("image/jpg", "jpg");
        mimeExtensions.put("image/jpeg", "jpg");

        OBJECT_MAPPER = JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                .configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false)
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                .build();
        SimpleModule simpleModule = new SimpleModule()
                .addSerializer(Value.class,
                        new JsonSerializer<>() {
                            @Override
                            public void serialize(Value value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                                if (value.isNilValue()) {
                                    jsonGenerator.writeNull();
                                } else {
                                    jsonGenerator.writeRaw(value.toJson());
                                }
                            }
                        })
                .addSerializer(byte[].class,
                        new JsonSerializer<>() {
                            @Override
                            public void serialize(byte[] value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                                if (value == null) {
                                    jsonGenerator.writeNull();
                                } else {
                                    jsonGenerator.writeString("<" + value.length + " bytes>");
                                }
                            }
                        });


        OBJECT_MAPPER.registerModule(simpleModule);
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
        try {
            return OBJECT_MAPPER.writeValueAsString(new FullGdsMessage(header, data));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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
        String extension = "unknown";
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

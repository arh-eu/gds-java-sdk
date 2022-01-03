
package hu.arheu.gds.message.data;

import java.util.HashMap;
import java.util.Map;


public enum MessageDataType {

    CONNECTION_0(0),
    CONNECTION_ACK_1(1),
    EVENT_2(2),
    EVENT_ACK_3(3),
    ATTACHMENT_REQUEST_4(4),
    ATTACHMENT_REQUEST_ACK_5(5),
    ATTACHMENT_RESPONSE_6(6),
    ATTACHMENT_RESPONSE_ACK_7(7),
    EVENT_DOCUMENT_8(8),
    EVENT_DOCUMENT_ACK_9(9),
    QUERY_REQUEST_10(10),
    QUERY_REQUEST_ACK_11(11),
    NEXT_QUERY_PAGE_12(12);

    private final int value;
    private static final Map<Integer, MessageDataType> map = new HashMap<>();

    MessageDataType(int value) {
        this.value = value;
    }

    static {
        for (MessageDataType messageDataType : MessageDataType.values()) {
            map.put(messageDataType.value, messageDataType);
        }
    }

    public int getValue() {
        return this.value;
    }

    public static MessageDataType valueOf(Integer value) {
        return map.get(value);
    }
}

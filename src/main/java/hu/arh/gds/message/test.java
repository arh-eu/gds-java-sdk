package hu.arh.gds.message;

import hu.arh.gds.message.data.MessageData;
import hu.arh.gds.message.data.MessageData0Connection;
import hu.arh.gds.message.header.MessageDataType;
import hu.arh.gds.message.header.MessageHeader;
import hu.arh.gds.message.util.MessageManager;
import hu.arh.gds.message.util.ReadException;
import hu.arh.gds.message.util.ValidationException;
import hu.arh.gds.message.util.WriteException;

import java.io.IOException;

public class test {

    public static void main(String[] args) throws IOException, ValidationException, WriteException, ReadException {

        MessageHeader header = MessageManager.createMessageHeaderBase("user", "870da92f-7fff-48af-825e-05351ef97acd", System.currentTimeMillis(), System.currentTimeMillis(), false, null, null, null, null, MessageDataType.CONNECTION_0);

        MessageData data = MessageManager.createMessageData0Connection(false, 1, false, null, "pass");

        byte[] message = MessageManager.createMessage(header, data);

        MessageHeader unpackedHeader = MessageManager.getMessageHeaderFromBinaryMessage(message);

        MessageData unpackedData = MessageManager.getMessageData(message);

        if(unpackedData.getTypeHelper().isConnectionMessageData0()) {
            MessageData0Connection connectionData = unpackedData.getTypeHelper().asConnectionMessageData0();
        }

    }
}

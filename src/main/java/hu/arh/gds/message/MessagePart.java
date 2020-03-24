/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package hu.arh.gds.message;

import java.io.IOException;
import java.util.Arrays;

import hu.arh.gds.message.util.ReadException;
import hu.arh.gds.message.util.ReaderHelper;
import hu.arh.gds.message.util.ValidationException;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

/**
 *
 * @author oliver.nagy
 */
public abstract class MessagePart {
    
    private byte[] binary;
    protected boolean cache;

    private int messageSize;

    protected void init() {
        //override this method
    }

    public MessagePart(byte[] binary, boolean cache) throws IOException, ReadException, ValidationException {
        this.messageSize = binary.length;
        init();
        Deserialize(binary, cache, true);
    }

    public MessagePart(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        this.messageSize = binary.length;
        init();
        Deserialize(binary, cache, isFullMessage);
    }
    
    public MessagePart() throws IOException {
        init();
    }
    
    public final byte[] getBinary() throws IOException, ValidationException {
        if(this.binary == null) {
            return Serialize();
        }
        return this.binary;
    }

    public int getMessageSize() {
        return this.messageSize;
    }

    protected final void Deserialize(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        if (binary.length > Integer.MAX_VALUE) {
            throw new ReadException(String.format("%s: Message size limit reached", this.getClass().getSimpleName()));
        }
        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(binary);
        if(isFullMessage) {
            int arrayHeader = ReaderHelper.unpackArrayHeader(unpacker, null, null, null);
            if (getMessagePartType() == MessagePartType.DATA) {
                unpacker.skipValue(arrayHeader - 1);
            }
        }
        int startOfMessagePart = (int) unpacker.getTotalReadBytes();
        UnpackValues(unpacker);
        int endOfMessagePart = (int) unpacker.getTotalReadBytes();
        if (cache) {
            this.binary = Arrays.copyOfRange(binary, startOfMessagePart, endOfMessagePart);
        } else {
            this.binary = null;
        }
        try {
            unpacker.close();
        } catch (IOException e) {
        }
        unpacker = null;
    }

    protected final byte[] Serialize() throws IOException, ValidationException {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        PackValues(packer);
        this.binary = packer.toByteArray();
        this.messageSize = this.binary.length;
        try {
            packer.close();
        } catch (IOException e) {
        }
        packer = null;
        return this.binary;
    }
    
    protected abstract void PackValues(MessageBufferPacker packer) throws IOException, ValidationException;
    protected abstract void UnpackValues(MessageUnpacker unpacker) throws ReadException, IOException, ValidationException;
    protected abstract MessagePartType getMessagePartType();
    protected abstract void checkContent();
}

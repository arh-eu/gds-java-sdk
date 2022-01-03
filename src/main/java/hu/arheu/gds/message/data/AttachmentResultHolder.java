
package hu.arheu.gds.message.data;

import hu.arheu.gds.message.util.GdsMessagePart;

import java.util.List;


public interface AttachmentResultHolder extends GdsMessagePart {

    enum Type {
        ATTACHMENT_REQUEST_ACK,
        ATTACHMENT_RESPONSE,
        ATTACHMENT_RESPONSE_ACK
    }

    List<String> getRequestIds();

    String getOwnerTable();

    String getAttachmentId();

    List<String> getOwnerIds();

    String getMeta();

    Long getTtl();

    Long getToValid();

    byte[] getAttachment();

    @Override
    default int getNumberOfPublicElements() {
        return 8;
    }
}

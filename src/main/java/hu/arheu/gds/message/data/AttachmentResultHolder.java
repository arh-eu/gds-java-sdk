package hu.arheu.gds.message.data;

import hu.arheu.gds.message.util.Packable;

import java.util.List;

public interface AttachmentResultHolder extends Packable {
    List<String> getRequestIds();
    String getOwnerTable();
    String getAttachmentId();
    List<String> getOwnerIds();
    String getMeta();
    Long getTtl();
    Long getToValid();
    byte[] getAttachment();
}

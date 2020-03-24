package hu.arh.gds.message.data;

import hu.arh.gds.message.data.impl.AckStatus;
import hu.arh.gds.message.util.Packable;
import hu.arh.gds.message.util.PublicElementCountable;

public interface AttachmentResponseAckResultHolder extends PublicElementCountable, Packable {
    AckStatus getStatus();
    AttachmentResultHolder getResult();
}

package hu.arheu.gds.message.data;

import hu.arheu.gds.message.data.impl.AckStatus;
import hu.arheu.gds.message.util.Packable;
import hu.arheu.gds.message.util.PublicElementCountable;

public interface AttachmentResponseAckResultHolder extends PublicElementCountable, Packable {
    AckStatus getStatus();
    AttachmentResultHolder getResult();
}

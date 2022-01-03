package hu.arheu.gds.message.data;

import hu.arheu.gds.message.data.impl.AckStatus;
import hu.arheu.gds.message.util.GdsMessagePart;

public interface AttachmentResponseAckResultHolder extends GdsMessagePart {
    /**
     * The status code associated with the current attachment
     *
     * @return the Status Code
     */
    AckStatus getStatus();

    /**
     * The actual attachment result for the current request.
     *
     * @return the result
     */
    AttachmentResultHolder getResult();

    @Override
    default int getNumberOfPublicElements() {
        return 2;
    }
}

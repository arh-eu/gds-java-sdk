package hu.arheu.gds.message.data;

import hu.arheu.gds.message.data.impl.AckStatus;
import hu.arheu.gds.message.util.GdsMessagePart;

public interface AttachmentRequestAckDataHolder extends GdsMessagePart {

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

    /**
     * If this message is sent from the client towards the GDS the value is {@code null}.
     * If the result contains the binary the value is zero.
     * Otherwise, the value denotes the remaining wait time of the GDS for the attachment before replying with error.
     *
     * @return the wait time
     */
    Long getRemainedWaitTimeMillis();

    @Override
    default int getNumberOfPublicElements() {
        return 3;
    }
}

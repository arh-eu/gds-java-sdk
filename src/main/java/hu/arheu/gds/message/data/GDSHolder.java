
package hu.arheu.gds.message.data;

import hu.arheu.gds.message.util.GdsMessagePart;

public interface GDSHolder extends GdsMessagePart {

    String getClusterName();

    String getGDSNodeName();

    @Override
    default int getNumberOfPublicElements() {
        return 2;
    }
}

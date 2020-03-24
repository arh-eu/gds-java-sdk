package hu.arh.gds.message.data;

import hu.arh.gds.message.util.Packable;
import hu.arh.gds.message.util.PublicElementCountable;

public interface GDSHolder extends PublicElementCountable, Packable {
    String getClusterName();
    String getGDSNodeName();
}

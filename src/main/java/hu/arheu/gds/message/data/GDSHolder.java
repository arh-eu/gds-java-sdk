package hu.arheu.gds.message.data;

import hu.arheu.gds.message.util.Packable;
import hu.arheu.gds.message.util.PublicElementCountable;

public interface GDSHolder extends PublicElementCountable, Packable {
    String getClusterName();
    String getGDSNodeName();
}

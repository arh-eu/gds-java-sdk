package hu.arh.gds.message.data;

import hu.arh.gds.message.data.impl.AckStatus;

public interface Ack {
    AckStatus getGlobalStatus();
    String getGlobalException();
}

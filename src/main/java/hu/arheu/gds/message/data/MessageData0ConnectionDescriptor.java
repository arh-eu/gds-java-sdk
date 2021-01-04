package hu.arheu.gds.message.data;

public interface MessageData0ConnectionDescriptor {
    Boolean getServeOnTheSameConnection();
    String getClusterName();    
    Integer getProtocolVersionNumber();    
    Boolean getFragmentationSupported();  
    Long getFragmentTransmissionUnit();
    String getPassword();
}

/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package hu.arh.gds.message.header;

import hu.arh.gds.message.util.PublicElementCountable;

import java.util.List;

/**
 *
 * @author oliver.nagy
 */
public interface MessageHeaderExtraFieldsDescriptor {
    
    List<String> getGdsNodeNamesInRequests();    
    List<String> getGdsNodeNamesInResponses();    
    String getConnectionHash();
    Boolean getIsServeOnTheSameConnection();
    Integer getHopCount();
    Long getInputBufferSize();
    Long getOutputBufferSize();
    Boolean getIsSecured();
    Boolean getFromClient();
}

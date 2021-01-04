/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package hu.arheu.gds.message.header;

import hu.arheu.gds.message.util.ReadException;
import hu.arheu.gds.message.util.ValidationException;

import java.io.IOException;

/**
 *
 * @author oliver.nagy
 */
public abstract class MessageHeaderBase extends MessageHeader implements MessageHeaderBaseDescriptor {
    
    public MessageHeaderBase(byte[] binary, boolean cache) throws IOException, ReadException, ValidationException {
        super(binary, cache);
    }

    public MessageHeaderBase(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        super(binary, cache, isFullMessage);
    }
    
    public MessageHeaderBase() throws IOException {
        
    }
    
}

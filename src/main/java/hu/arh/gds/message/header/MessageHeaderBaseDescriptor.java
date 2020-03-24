/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.arh.gds.message.header;


import hu.arh.gds.message.util.PublicElementCountable;

/**
 *
 * @author oliver.nagy
 */
public interface MessageHeaderBaseDescriptor {
    
    String getUserName();   
    String getMessageId();
    Long getCreateTime();
    Long getRequestTime();
    Boolean getIsFragmented();
    Boolean getFirstFragment();
    Boolean getLastFragment();
    Long getOffset();
    Long getFullDataSize();
    MessageDataType getDataType();   
}

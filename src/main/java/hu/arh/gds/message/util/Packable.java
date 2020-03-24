/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.arh.gds.message.util;

import java.io.IOException;
import org.msgpack.core.MessageBufferPacker;

/**
 *
 * @author llacz
 */
public interface Packable {

    void packContent(MessageBufferPacker packer) throws IOException, ValidationException;
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.arheu.gds.message.util;

import org.msgpack.core.MessageBufferPacker;

import java.io.IOException;

/**
 * Indicates an object which can be sent via MessagePack
 *
 * @author llacz
 */
public interface Packable {

    /**
     * Packs the current value into the specified MessagePack buffer.
     *
     * @param packer the Buffer to be packed into.
     * @throws IOException         if the writing fails
     * @throws ValidationException if the type of this value is invalid
     */
    void packContent(MessageBufferPacker packer) throws IOException, ValidationException;
}

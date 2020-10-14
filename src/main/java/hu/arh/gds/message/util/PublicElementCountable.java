/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.arh.gds.message.util;

/**
 * Used to indicate that these values contain specific number of public elements, that should be used to pack this
 * value into / as an array.
 *
 * @author llacz
 */
public interface PublicElementCountable {

    /**
     * Calculates and returns the number of elements this instance will pack when serialized into MessagePack format.
     *
     * @return the number of public elements this value has.
     */
    int getNumberOfPublicElements();
}

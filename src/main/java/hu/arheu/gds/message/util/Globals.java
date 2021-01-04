/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.arheu.gds.message.util;

/**
 * @author oliver.nagy
 */
public class Globals {

    private Globals() {
    }

    /**
     * array header prefix for less than 16 elements
     */
    public static final String fixarray = "1001";
    /**
     * array header prefix for up to (2^16-1) elements
     */
    public static final String array16 = "11011100";

    /**
     * array header prefix for up to (2^32-1) elements
     */
    public static final String array32 = "11011101";

    public static final int BASE_HEADER_FIELDS_NUMBER = 10;
    public static final int DATA_FIELDS_NUMBER = 1;
    public static final int EXTRA_HEADER_FIELDS_NUMBER = 9;
}

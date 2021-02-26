package com.adt.kotlin.kwikcheck.argument

/**
 * An argument used in a property that may have undergone shrinking
 *   following falsification.
 *
 * @author	                    Ken Barclay
 * @since                       April 2020
 */



class Argument<T>(val value: T, val shrinks: Int, val label: String = "") {

    override fun toString(): String ="$value"

}   // Argument

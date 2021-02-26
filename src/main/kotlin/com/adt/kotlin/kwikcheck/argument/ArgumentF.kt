package com.adt.kotlin.kwikcheck.argument

/**
 * An argument used in a property that may have undergone shrinking
 *   following falsification.
 *
 * @author	                    Ken Barclay
 * @since                       April 2020
 */



object ArgumentF {

    /**
     * Factory function creating an Argument.
     *
     * @param value             the shrinked property
     * @param shrinks           the number of shrinks undergone
     * @return                  new Argument instance
     */
    fun <T> arg(value: T, shrinks: Int): Argument<T> = Argument(value, shrinks)

}   // ArgumentF

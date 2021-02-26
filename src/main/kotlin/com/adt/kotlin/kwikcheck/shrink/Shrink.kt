package com.adt.kotlin.kwikcheck.shrink

/**
 * Represents a shrinking strategy over the given type parameter if that type can
 *   be represented as a tree structure. This is used in falsification to produce
 *   the smallest counter-example, rather than the first counter-example.
 *
 * @author	                    Ken Barclay
 * @since                       April 2020
 */

import com.adt.kotlin.kats.data.immutable.list.List



typealias ShrinkFunc<A> = (A) -> List<A>

class Shrink<A>(val func: ShrinkFunc<A>) {

    /**
     * Return a shrink of the given argument.
     *
     * @param t                 the argument to shrink
     * @return                  a shrink of the given argument
     */
    fun apply(a: A): List<A> = func(a)

    /**
     * Transform a Shrink<A> to a Shrink<B> where A and B are two isomorphic
     *   types whose relationship is described by the provided transformation
     *   functions.
     *
     * @param f                 a transformation from this shrink type to the new shrink type
     * @param g                 a transformation from the new shrink type to this shrink type
     * @return                  a shrink from this shrink and the given symmetric transformations
     */
    fun <B> map2(f: (A) -> B, g: (B) -> A): Shrink<B> =
        Shrink{b: B -> this.apply(g(b)).map(f)}

    /**
     * Create a new shrink that only produces values satisfying the given
     *   condition.
     *
     * @param predicate         criteria to be met
     * @return                  the conditional shrink
     */
    fun filter(predicate: (A) -> Boolean): Shrink<A> =
        Shrink{a: A -> this.shrink(a).filter(predicate)}

    /**
     * Return a shrink of the given argument.
     *
     * @param t                 the argument to shrink
     * @return                  a shrink of the given argument
     */
    fun shrink(a: A): List<A> = func(a)

}   // Shrink

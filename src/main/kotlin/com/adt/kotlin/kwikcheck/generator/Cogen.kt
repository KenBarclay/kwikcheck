package com.adt.kotlin.kwikcheck.generator

/**
 * Transform a type and generator to produce a new generator.
 *
 * @author	                    Ken Barclay
 * @since                       April 2020
 */



abstract class Cogen<A> {

    /**
     * Transform the given value and generator to a new generator with a high
     *   probability of being independent.
     *
     * @param a                 the value to produce the generator from
     * @param gen               the generator to produce the new generator from
     * @return                  a new generator
     */
    abstract fun <B> cogen(value: A, gen: Gen<B>): Gen<B>

    /**
     * A curried version of the member cogen.
     *
     * @param a                 the value to produce the generator from
     * @return                  a function when given a generator delivers the new generator
     */
    fun <B> cogen(a: A): (Gen<B>) -> Gen<B> =
        {gen: Gen<B> -> this.cogen(a, gen)}

    /**
     * Compose the given function with this Cogen to produce a new Cogen.
     *
     * @param f                 the compose function
     * @return                  a new Cogen composed with the given function
     */
    fun <B> compose(f: (B) -> A): Cogen<B> {
        val self: Cogen<A> = this
        return object: Cogen<B>() {
            override fun <X> cogen(value: B, gen: Gen<X>): Gen<X> =
                self.cogen(f(value), gen)
        }
    }   // compose

    /**
     * Contramap the given function with this Cogen to produce a new Cogen.
     *
     * @param f                 the compose function
     * @return                  a new Cogen contramapped with the given function
     */
    fun <B> contramap(f: (B) -> A): Cogen<B> {
        val self: Cogen<A> = this
        return object: Cogen<B>() {
            override fun <X> cogen(value: B, gen: Gen<X>): Gen<X> =
                self.cogen(f(value), gen)
        }
    }   // contramap

}   // Cogen

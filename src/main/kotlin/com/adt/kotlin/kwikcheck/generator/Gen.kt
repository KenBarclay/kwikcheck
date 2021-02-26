package com.adt.kotlin.kwikcheck.generator

/**
 * A generator for random values of the type parameter. A generator is a
 *   function from an Int (the size) to a function from a Rand (the random
 *   number generator) to a value of type A. The function is used lazily so
 *   that, for example, member function map applies the transformation parameter
 *   to the underlying function without executing anything.
 *
 * Several standard generators are provided by the GenF object, such
 *   as genAlphaStr which generates strings of alphabetic characters.
 *
 * @author	                    Ken Barclay
 * @since                       April 2020
 *
 * @param A                     the type of random value produced by the generator
 * @param func                  the lazy function representation
 */

import com.adt.kotlin.kats.data.immutable.list.List
import com.adt.kotlin.kats.data.immutable.list.ListF

import com.adt.kotlin.kwikcheck.random.Rand
import com.adt.kotlin.kwikcheck.random.RandF



typealias RandFunc<A> = (Int) -> (Rand) -> A    // Int is the size

class Gen<out A> internal constructor(private val func: RandFunc<A>) {

    /**
     * Apply the given size and random generator to produce a value of type A.
     *   A synonym for gen.
     *
     * @param size              the size used to produce the random value
     * @param rand              the random generator used to produce the random value
     * @return                  the generated random value
     */
    fun apply(size: Int, rand: Rand): A = func(size)(rand)

    /**
     * Apply the given size and random generator to produce a value of type A.
     *   A synonym for apply.
     *
     * @param size              the size used to produce the random value
     * @param rand              the random generator used to produce the random value
     * @return                  the generated random value
     */
    fun gen(size: Int, rand: Rand): A = func(size)(rand)

    /**
     * Apply the given size and random generator to produce a value of type A.
     *   A synonym for apply and gen.
     *
     * @param size              the size used to produce the random value
     * @param rand              the random generator used to produce the random value
     * @return                  the generated random value
     */
    operator fun invoke(size: Int, rand: Rand): A = func(size)(rand)

    /**
     * Return a generator that produces random values that meet the given predicate.
     *
     * @param predicate         the predicate for the random values produced by the generator
     * @return                  a new generator that produces random values that meet the given predicate
     */
    fun filter(predicate: (A) -> Boolean): Gen<A> =
        Gen{size: Int ->
            {rand: Rand ->
                var generated: A

                do {
                    generated = this.apply(size, rand)
                } while (!predicate(generated))

                generated
            }
        }  // filter

    /**
     * Map the given function over this generator.
     *
     * @param f                 the function to map across this generator
     * @return                  the new generator
     */
    fun <B> map(f: (A) -> B): Gen<B> =
        Gen{size: Int ->
            {rand: Rand ->
                f(this.apply(size, rand))
            }
        }   // map

    /**
     * Override the size parameter. Return a generator which uses the
     *   given size instead of the runtime size parameter.
     *
     * @param sz                the new size of the generator
     * @return                  a new generator
     */
    fun resize(sz: Int): Gen<A> {
        return if (sz < 0)
            throw IllegalArgumentException("Gen.resize: negative size: $sz")
        else
            Gen{_: Int ->
                {rand: Rand ->
                    this.apply(sz, rand)
                }
            }
    }   // resize

    /**
     * Generate sample values by using default parameters.
     *
     * @return                  a list of 19 sample values from this generator
     */
    fun samples(): List<A> {
        val range: List<Int> = ListF.cons(0, ListF.range(2, 20))    // 19 elements
        val genRange: List<Gen<A>> = range.map{size -> this.resize(size)}
        val genSequence: Gen<List<A>> = GenF.sequence(genRange)
        return genSequence.gen(30, RandF.standard)
    }   // samples



// ---------- categorical functions -----------------------

    /**
     * Apply the function to the content of this generator producing a
     *   new generator (functor).
     *
     * @param f                 the function to apply to this generator
     * @return                  the new generator after the function application
     */
    fun <B> fmap(f: (A) -> B): Gen<B> = this.map(f)

    /**
     * Apply the function wrapped in a generator context to the content of this
     *   value also wrapped in a generator context (applicative).
     *
     * @param f                 the wrapped function to apply
     * @return                  the new generator
     */
    fun <B> ap(f: Gen<(A) -> B>): Gen<B> =
        this.bind{_: A ->
            f.bind{g: (A) -> B ->
                Gen{size: Int -> {rand: Rand -> g(this.apply(size, rand)) }}
            }
        }   // ap

    /**
     * Bind the given function across this generator producing a new generator (monad).
     *
     * @param f                 the function to bind across this generator
     * @return                  the new generator after binding the given function
     */
    fun <B> bind(f: (A) -> Gen<B>): Gen<B> =
        Gen{size: Int -> {rand: Rand -> f(gen(size, rand)).gen(size, rand)}}

    /**
     * Sequentially compose two actions, discarding any value produced by the first;
     *   like sequencing operators (such as the semicolon) in imperative languages (monad).
     *
     * @param gen               the new generator
     */
    fun <B> then(gen: Gen<B>): Gen<B> = this.bind{_: A -> gen}

}   // Gen

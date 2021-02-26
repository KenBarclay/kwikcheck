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
 */


/**
 * Functions to support an applicative style of programming.
 *
 * Examples:
 *   {a: A -> ... B value} fmap genA ==> Gen<B>
 *   {a: A -> {b: B -> ... C value}} fmap genA ==> Gen<(B) -> C>
 *   {a: A -> {b: B -> ... C value}} fmap genA ap genB ==> Gen<C>
 */
infix fun <A, B> ((A) -> B).fmap(gen: Gen<A>): Gen<B> =
    gen.fmap(this)

infix fun <A, B> Gen<(A) -> B>.ap(gen: Gen<A>): Gen<B> =
    gen.ap(this)

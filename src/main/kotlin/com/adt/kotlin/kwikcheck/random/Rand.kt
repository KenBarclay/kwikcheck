package com.adt.kotlin.kwikcheck.random

/**
 * A random number generator.
 *
 * @author	                    Ken Barclay
 * @since                       April 2020
 *
 * @param f                     the integer random generator
 * @param g                     the double random generator
 * @param reSeed                function that creates a reseeded Rand
 */

import com.adt.kotlin.kats.data.immutable.option.Option
import com.adt.kotlin.kats.data.immutable.option.OptionF.none
import com.adt.kotlin.kats.data.immutable.option.OptionF.some



typealias RandIntFunc = (Option<Long>) -> (Int) -> (Int) -> Int
typealias RandDoubleFunc = (Option<Long>) -> (Double) -> (Double) -> Double

class Rand internal constructor(private val f: RandIntFunc, private val g: RandDoubleFunc, private val reSeed: (Long) -> Rand) {

    /**
     * Randomly choose an Int value between the given Int range (inclusive).
     *
     * @param seed              the seed to use for random generation
     * @param from              the minimum value to choose
     * @param to                the maximum value to choose
     * @return                  a random value in the given range
     */
    fun choose(seed: Long, from: Int, to: Int): Int = f(some(seed))(from)(to)

    /**
     * Randomly choose an Int value between the given Int range (inclusive).
     *
     * @param from              the minimum value to choose
     * @param to                the maximum value to choose
     * @return                  a random value in the given range
     */
    fun choose(from: Int, to: Int): Int = f(none())(from)(to)

    /**
     * Randomly choose a Long value between the given Long range (inclusive).
     *
     * @param from              the minimum value to choose
     * @param to                the maximum value to choose
     * @return                  a random value in the given range
     */
    fun choose(from: Long, to: Long): Long =
        g(none())(from.toDouble())(to.toDouble()).toLong()

    /**
     * Randomly choose a Double value between the given Double range (inclusive).
     *
     * @param seed              the seed to use for random generation
     * @param from              the minimum value to choose
     * @param to                the maximum value to choose
     * @return                  a random value in the given range
     */
    fun choose(seed: Long, from: Double, to: Double): Double = g(some(seed))(from)(to)

    /**
     * Randomly chooses a Double value between the given Double range (inclusive).
     *
     * @param from              the minimum value to choose
     * @param to                the maximum value to choose
     * @return                  a random value in the given range
     */
    fun choose(from: Double, to: Double): Double = g(none())(from)(to)

    /**
     * Deliver a new random generator with a new seed.
     *
     * @param seed              the seed of the new random generator
     * @return                  a random generator with the given seed
     */
    fun reseed(seed: Long): Rand = reSeed(seed)

}   // Rand

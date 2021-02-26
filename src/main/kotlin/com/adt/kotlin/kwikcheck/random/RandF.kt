package com.adt.kotlin.kwikcheck.random

/**
 * A random number generator.
 *
 * @author	                    Ken Barclay
 * @since                       April 2020
 */

import java.util.Random
import kotlin.math.max
import kotlin.math.min



object RandF {

    /**
     * Construct a random generator from the given functions that supply a range to produce a
     *   result.
     *
     * @param f                     the integer random generator
     * @param g                     the floating-point random generator
     * @return                      function that creates a reseeded Rand
     */
    fun rand(f: RandIntFunc, g: RandDoubleFunc, reSeed: (Long) -> Rand): Rand = Rand(f, g, reSeed)

    /**
     * A standard random number generator that employs the standard library class
     *   Random to generate a stream of pseudorandom numbers.
     */
    val standard: Rand = createStandard(Random())



// ---------- implementation ------------------------------

    /**
     * A standard random number generator that employs the standard library class
     *   Random to generate a stream of pseudorandom numbers.
     */
    fun createStandard(defaultRandom: Random): Rand =
        rand(
            {opt -> {from -> {to -> standardChooseInt(opt.option(defaultRandom, {_ -> Random()}), from, to)}}},
            {opt -> {from -> {to -> standardChooseDouble(opt.option(defaultRandom, {_ -> Random()}), from, to)}}},
            {seed -> createStandard(Random(seed))}
        )   // createStandard

    /**
     * Return a uniformly distributed Int value between min[from, to] (inclusive)
     *   and max[from, to] (inclusive).
     */
    private fun standardChooseInt(random: Random, from: Int, to: Int): Int {
        return if (from != to) {
            val min: Int = min(from, to)
            val max: Int = max(from, to)
            val range: Long = (1L + max) - min
            val bound: Long = Long.MAX_VALUE - (Long.MAX_VALUE % range)
            var r: Long = random.nextLong().and(Long.MAX_VALUE)
            while (r >= bound) {
                r = random.nextLong().and(Long.MAX_VALUE)
            }
            (r % range + min).toInt()
        } else
            from
    }   // standardChooseInt

    /**
     * Return a uniformly distributed Double value between min[from, to] (inclusive)
     *   and max[from, to] (exclusive).
     */
    private fun standardChooseDouble(random: Random, from: Double, to: Double): Double {
        val min: Double = min(from, to)
        val max: Double = max(from, to)
        return (max - min) * random.nextDouble() + min
    }   // standardChooseDouble

}   // RandF

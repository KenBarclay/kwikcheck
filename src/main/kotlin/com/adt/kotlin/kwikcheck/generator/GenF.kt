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

import com.adt.kotlin.kats.data.immutable.either.Either
import com.adt.kotlin.kats.data.immutable.either.EitherF.left
import com.adt.kotlin.kats.data.immutable.either.EitherF.right
import com.adt.kotlin.kats.data.immutable.identity.Identity
import com.adt.kotlin.kats.data.immutable.identity.IdentityF.identity


import com.adt.kotlin.kats.data.immutable.list.List
import com.adt.kotlin.kats.data.immutable.list.List.Nil
import com.adt.kotlin.kats.data.immutable.list.List.Cons
import com.adt.kotlin.kats.data.immutable.list.ListF
import com.adt.kotlin.kats.data.immutable.list.ListF.cons
import com.adt.kotlin.kats.data.immutable.list.ListF.nil
import com.adt.kotlin.kats.data.immutable.list.ListF.replicate
import com.adt.kotlin.kats.data.immutable.list.append

import com.adt.kotlin.kats.data.immutable.map.Map
import com.adt.kotlin.kats.data.immutable.map.MapF
import com.adt.kotlin.kats.data.immutable.map.insert
import com.adt.kotlin.kats.data.immutable.map.union

import com.adt.kotlin.kats.data.immutable.hamt.Map as HAMTMap
import com.adt.kotlin.kats.data.immutable.hamt.MapF as HAMTMapF

import com.adt.kotlin.kats.data.immutable.nel.NonEmptyList
import com.adt.kotlin.kats.data.immutable.nel.NonEmptyListF.nonEmptyList

import com.adt.kotlin.kats.data.immutable.option.Option
import com.adt.kotlin.kats.data.immutable.option.Option.None
import com.adt.kotlin.kats.data.immutable.option.Option.Some
import com.adt.kotlin.kats.data.immutable.option.OptionF.some
import com.adt.kotlin.kats.data.immutable.rbtree.RedBlackTree

import com.adt.kotlin.kats.data.immutable.set.Set
import com.adt.kotlin.kats.data.immutable.set.SetF

import com.adt.kotlin.kats.data.immutable.stream.Stream
import com.adt.kotlin.kats.data.immutable.stream.StreamF

import com.adt.kotlin.kats.data.immutable.tree.Tree
import com.adt.kotlin.kats.data.immutable.tree.TreeF
import com.adt.kotlin.kats.data.immutable.tri.Try
import com.adt.kotlin.kats.data.immutable.tri.TryF

import com.adt.kotlin.kats.data.immutable.validation.Validation
import com.adt.kotlin.kats.data.immutable.validation.ValidationF

import com.adt.kotlin.kwikcheck.random.Rand

import kotlin.collections.List as KList

import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import kotlin.math.max
import kotlin.math.min



object GenF {

    /**
     * Return a generator that uses the given function. This is the factory
     *   function for the generator.
     *
     * @param f                 the function to use for this generator
     * @return                  a new generator that uses the given function
     */
    fun <A> gen(f: RandFunc<A>): Gen<A> = Gen(f)

    /**
     * Return a generator that produces random Int values in the given inclusive
     *   range.
     *
     * @param from              the start of the range (inclusive)
     * @param to                the end of the range (inclusive)
     * @return                  a new generator that produces values between the given inclusive range
     */
    fun choose(from: Int, to: Int): Gen<Int> =
        Gen{_: Int ->
            {rand: Rand ->
                val min: Int = min(from, to)
                val max: Int = max(from, to)
                rand.choose(min, max)
            }
        }   // choose

    /**
     * Return a generator that produces random Long values in the given inclusive
     *   range.
     *
     * @param from              the start of the range (inclusive)
     * @param to                the end of the range (inclusive)
     * @return                  a new generator that produces values between the given inclusive range
     */
    fun choose(from: Long, to: Long): Gen<Long> =
        Gen{_: Int ->
            {rand: Rand ->
                val min: Long = min(from, to)
                val max: Long = max(from, to)
                rand.choose(min, max)
            }
        }   // choose

    /**
     * Return a generator that produces random Double values in the given inclusive
     *   range.
     *
     * @param from              the start of the range (inclusive)
     * @param to                the end of the range (inclusive)
     * @return                  a new generator that produces values between the given inclusive range
     */
    fun choose(from: Double, to: Double): Gen<Double> =
        Gen{_: Int ->
            {rand: Rand ->
                val min: Double = min(from, to)
                val max: Double = max(from, to)
                rand.choose(min, max)
            }
        }   // choose

    /**
     * Return a generator that produces random Calendar values in the given inclusive
     *   range.
     *
     * @param from              the start of the range (inclusive)
     * @param to                the end of the range (inclusive)
     * @return                  a new generator that produces values between the given inclusive range
     */
    fun choose(from: Calendar, to: Calendar): Gen<Calendar> {
        val fromMillis: Long = from.timeInMillis
        val toMillis: Long = to.timeInMillis
        return choose(fromMillis, toMillis).map{millis ->
            val calendar: Calendar = Calendar.getInstance()
            calendar.timeInMillis = millis
            calendar
        }
    }   // choose

    /**
     * Return a generator that produces a value from the given arguments.
     *   The input sequence must be non-empty.
     *
     * @param ts                the values that the returned generator may produce
     * @return                  a new generator
     */
    fun <A> elements(vararg ts: A): Gen<A> =
        if (ts.isEmpty())
            fail("GenF.elements: empty sequence")
        else
            choose(0, ts.size - 1).map{idx -> ts[idx]}

    fun <A> elements(gs: List<A>): Gen<A> =
        if (gs.isEmpty())
            fail("GenF.elements: used with empty list")
        else
            choose(0, gs.size() - 1).map{idx -> gs[idx]}

    /**
     * Return a generator that never returns a value.
     *
     * @return                  a new generator
     */
    fun <A> fail(message: String): Gen<A> =
        gen{_: Int ->
            {_: Rand ->
                throw Error(message)
            }
        }   // fail

    /**
     * Choose one of the given generators with the weighted random distribution.
     *   The input list must be non-empty.
     *
     * @param gs                the pairs of frequency and generator from which to return values in the generator
     * @return                  a new generator
     */
    fun <A> frequency(gs: List<Pair<Int, Gen<A>>>): Gen<A> {
        tailrec fun <A> pick(m: Int, gs: List<Pair<Int, Gen<A>>>): Gen<A> {
            return if(gs.isEmpty())
                fail("GenF.frequency: used with empty list")
            else {
                val hd: Pair<Int, Gen<A>> = gs.head()
                val tl: List<Pair<Int, Gen<A>>> = gs.tail()
                val freq: Int = hd.first
                val gen: Gen<A> = hd.second
                if (m <= freq)
                    gen
                else
                    pick(m - freq, tl)
            }
        }   // pick

        val sum: Int = gs.foldLeft(0){tot, pr -> tot + pr.first}
        return choose(1, sum).bind{n: Int -> pick(n, gs)}
    }   // frequency

    /**
     * Choose one of the given generators with the weighted random distribution.
     *   The input list must be non-empty.
     *
     * @param gs                the pairs of frequency and generator from which to return values in the generator
     * @return                  a new generator
     */
    fun <A> frequency(vararg gs: Pair<Int, Gen<A>>): Gen<A> =
        frequency(ListF.fromArray(gs))

    /**
     * Choose one of the produced generators with a weighted random distribution.
     *   The immutable input list must be non-empty.
     *
     * @param gs                the pairs of frequency and value from which to return values in the generator
     * @return                  a new generator
     */
    fun <A> frequencyA(gs: List<Pair<Int, A>>): Gen<A> {
        val gss: List<Pair<Int, Gen<A>>> = gs.foldLeft(nil()){list ->
            {pair: Pair<Int, A> ->
                cons(Pair(pair.first, value(pair.second)), list)
            }
        }
        return frequency(gss)
    }   // frequencyA

    /**
     * Choose one of the given generators with a weighted random distribution.
     *   The input list must be non-empty.
     *
     * @param gs                the pairs of frequency and generator from which to return values in the generator
     * @return                  a new generator
     *
    fun <A> frequency(gs: KList<Pair<Int, Gen<A>>>): Gen<A> =
    frequency(ListF.from(gs))
     ***/

    /**
     * Choose one of the produced generators with a weighted random distribution.
     *   The immutable input list must be non-empty.
     *
     * @param gs                the pairs of frequency and value from which to return values in the generator
     * @return                  a new generator
     *
    fun <A> frequencyAK(gs: KList<Pair<Int, A>>): Gen<A> =
    frequencyA(ListF.from(gs))
     ***/

    /**
     * Choose one of the produced generators with a weighted random distribution.
     *   The immutable input list must be non-empty.
     *
     * @param gs                the pairs of frequency and value from which to return values in the generator
     * @return                  a new generator
     */
    fun <A> frequencyA(vararg gs: Pair<Int, A>): Gen<A> =
        frequencyA(ListF.fromArray(gs))

    /**
     * Return a generator selected randomly from the given values.
     *   The input list must be non-empty.
     *
     * @param ts                the list of values
     * @return                  a generator that produces values from one of the given values
     */
    fun <A> oneOf(vararg ts: A): Gen<A> =
        if (ts.isEmpty())
            fail("GenF.oneOf: used with empty sequence")
        else
            choose(0, ts.size - 1).bind{n -> value(ts[n])}

    /**
     * Return a generator selected randomly from the given generators.
     *   The input list must be non-empty.
     *
     * @param gs                the list of generators to produce a value from
     * @return                  a generator that produces values from one of the given generators on subsequent requests
     */
    fun <A> oneOf(gs: List<Gen<A>>): Gen<A> =
        if(gs.isEmpty())
            fail("GenF.oneOf: used with empty list")
        else
            choose(0, gs.size() - 1).bind{n -> gs[n]}

    /**
     * Return a generator selected randomly from the given generators.
     *   The input list must be non-empty.
     *
     * @param gs                the list of generator values
     * @return                  a generator that produces values from one of the given values
     */
    fun <A> oneOf(vararg gs: Gen<A>): Gen<A> =
        if (gs.isEmpty())
            fail("GenF.oneOf: used with empty list of generators")
        else
            choose(0, gs.size - 1).bind{n -> gs[n]}

    /**
     * Construct a generator that can access its construction arguments - size
     *   and random generator.
     *
     * @param f                 the function that constructs the generator with its arguments
     * @return                  a new generator
     */
    fun <A> parameterised(f: (Int) -> (Rand) -> Gen<A>): Gen<A> =
        Gen{size: Int -> {rand: Rand -> f(size)(rand).gen(size, rand)}}

    /**
     * Return a generator for a function type.
     *
     * @param f                 the function to promote to a generator of functions
     * @return                  a generator for functions
     */
    fun <A, B> promote(f: (A) -> Gen<B>): Gen<(A) -> B> =
        Gen{size: Int -> {rand: Rand -> {a: A -> f(a).gen(size, rand)}}}

    /**
     * Sequence the given generators through a bind operation transforming
     *   a list of generators into a generator of list values.
     *
     * @param gs                the generators to sequence
     * @return                  a generator of lists after sequencing the given generators
     */
    fun <A> sequence(gs: List<Gen<A>>): Gen<List<A>> =
        Gen{size: Int -> {rand: Rand -> gs.map{g -> g.gen(size, rand)}}}

    /**
     * Sequence the given generator the given number of times through a bind
     *   operation transforming a generator into a generator of list values.
     *
     * @param n                 the number of times to sequence the given generator
     * @param gen               the generator sequence
     * @return                  a generator of lists after sequencing the given generator
     */
    fun <A> sequence(n: Int, gen: Gen<A>): Gen<List<A>> =
        sequence(replicate(n, gen))

    /**
     * Sequence the given generators through a bind operation transforming
     *   a list of generators into a generator of list values.
     *
     * @param gs                the generators to sequence
     * @return                  a generator of lists after sequencing the given generators
     */
    fun <A> sequenceK(gs: KList<Gen<A>>): Gen<KList<A>> =
        Gen{size: Int -> {rand: Rand -> gs.map{g -> g.gen(size, rand)}}}

    /**
     * Sequence the given generator the given number of times through a bind
     *   operation transforming a generator into a generator of list values.
     *
     * @param n                 the number of times to sequence the given generator
     * @param gen               the generator sequence
     * @return                  a generator of lists after sequencing the given generator
     */
    fun <A> sequenceK(n: Int, gen: Gen<A>): Gen<KList<A>> =
        sequenceK(Array(n){_ -> gen}.toList())

    /**
     * Construct a generator that can access its size construction arguments.
     *
     * @param f                 the function that constructs the generator with its size argument
     * @return                  a new generator
     */
    fun <A> sized(f: (Int) -> Gen<A>): Gen<A> =
        Gen{size: Int -> {rand: Rand -> f(size).gen(size, rand)}}

    /**
     * Return a generator that always produces the given value.
     *
     * @param a                 the value to always produce
     * @return                  a generator that always produces the given value
     */
    fun <A> value(a: A): Gen<A> =
        Gen{_: Int -> {_: Rand -> a}}

    fun <A> constant(a: A): Gen<A> = value(a)

    /**
     * A generator for functions.
     *
     * @param cogenA            the cogen for the function domain
     * @param genB              the generator for the function codomain
     * @return                  a generator for functions
     */
    fun <A, B> genF(cogenA: Cogen<A>, genB: Gen<B>): Gen<(A) -> B> =
        promote{a -> cogenA.cogen(a, genB)}



// ---------- predefined generators -----------------------

    /**
     * An implementation for integer values.
     */
    val genInt: Gen<Int>
        get() = sized{n -> choose(-n, +n)}

    fun genInt(from: Int, to: Int): Gen<Int> = choose(from, to)

    /**
     * An implementation for positive integer values.
     */
    val genPosInt: Gen<Int>
        get() = sized{n -> choose(0, n)}

    fun genPosInt(from: Int, to: Int): Gen<Int> =
        if (from < 0)
            throw IllegalArgumentException("GenF.genPosInt: negative start: $from")
        else if (to < 0)
            throw IllegalArgumentException("GenF.genPosInt: negative end: $to")
        else
            choose(from, to)

    /**
     * An implementation for positive integer values not including zero.
     */
    val genPosInt1: Gen<Int>
        get() = choose(1, Int.Companion.MAX_VALUE)////sized{n -> choose(1, n)}

    fun genPosInt1(from: Int, to: Int): Gen<Int> =
        if (from < 1)
            throw IllegalArgumentException("GenF.genPosInt1: negative or zero start: $from")
        else if (to < 1)
            throw IllegalArgumentException("GenF.genPosInt1: negative or zero end: $to")
        else
            choose(from, to)

    /**
     * An implementation for boolean values.
     */
    val genBoolean: Gen<Boolean>
        get() = elements(false, true)

    /**
     * An implementation for byte values.
     */
    val genByte: Gen<Byte>
        get() = genInt.map{n: Int -> n.toByte()}

    /**
     * An implementation for character values.
     */
    val genChar: Gen<Char>
        get() = choose(0, 65536).map{n: Int -> n.toChar()}

    /**
     * An implementation for double values.
     */
    val genDouble: Gen<Double>
        get() = sized{n: Int -> choose((-n).toDouble(), (+n).toDouble())}

    fun genDouble(from: Double, to: Double): Gen<Double> = choose(from, to)

    /**
     * An implementation for positive double values.
     */
    val genPosDouble: Gen<Double>
        get() = sized{n: Int -> choose(0.0, n.toDouble())}

    fun genPosDouble(from: Double, to: Double): Gen<Double> =
        if (from < 0.0)
            throw IllegalArgumentException("GenF.genPosDouble: negative start: $from")
        else if (to < 0.0)
            throw IllegalArgumentException("GenF.genPosDouble: negative end: $to")
        else
            choose(from, to)

    /**
     * An implementation for float values.
     */
    val genFloat: Gen<Float>
        get() = genDouble.map{d: Double -> d.toFloat()}

    fun genFloat(from: Float, to: Float): Gen<Float> =
        genDouble(from.toDouble(), to.toDouble()).map{d: Double -> d.toFloat()}

    /**
     * An implementation for positive float values.
     */
    val genPosFloat: Gen<Float>
        get() = genPosDouble.map{d: Double -> d.toFloat()}

    fun genPosFloat(from: Float, to: Float): Gen<Float> =
        if (from < 0.0F)
            throw IllegalArgumentException("GenF.genPosFloat: negative start: $from")
        else if (to < 0.0F)
            throw IllegalArgumentException("GenF.genPosFloat: negative end: $to")
        else
            genDouble(from.toDouble(), to.toDouble()).map{d: Double -> d.toFloat()}

    /**
     * An implementation for long values.
     */
    val genLong: Gen<Long>
        get() = genInt.bind{n1: Int ->
            genInt.bind{n2: Int ->
                gen{_: Int ->
                    {_: Rand ->
                        (n1.toLong() shl 32) and n2.toLong()
                    }
                }
            }
        }   // genLong

    fun genLong(from: Long, to: Long): Gen<Long> = choose(from, to)

    /**
     * An implementation for positive long values.
     */
    val genPosLong: Gen<Long>
        get() = sized{n -> choose(0L, n.toLong())}

    fun genPosLong(from: Long, to: Long): Gen<Long> =
        if (from < 0L)
            throw IllegalArgumentException("GenF.genPosLong: negative start: $from")
        else if (to < 0L)
            throw IllegalArgumentException("GenF.genPosLong: negative end: $to")
        else
            choose(from, to)

    /**
     * An implementation for positive long values.
     */
    val genPosLong1: Gen<Long>
        get() = sized{n -> choose(1L, n.toLong())}

    fun genPosLong1(from: Long, to: Long): Gen<Long> =
        if (from < 1L)
            throw IllegalArgumentException("GenF.genPosLong1: negative or zero start: $from")
        else if (to < 1L)
            throw IllegalArgumentException("GenF.genPosLong1: negativeor zero end: $to")
        else
            choose(from, to)

    /**
     * An implementation for short values.
     */
    val genShort: Gen<Short>
        get() = genInt.map{n: Int -> n.toShort()}

    /**
     * An implementation for big integer values.
     */
    val genBigInteger: Gen<BigInteger>
        get() = genArray(genByte).bind{bytes: Array<Byte> ->
            value(BigInteger(bytes.toByteArray()))
        }

    /**
     * An implementation for big decimal values.
     */
    val genBigDecimal: Gen<BigDecimal>
        get() = genBigInteger.map{bigInt -> bigInt.toBigDecimal()}



// ---------- character generators ------------------------

    /**
     * Generates a numerical character (digit).
     *
     * genNumChar:: Gen[Char]
     */
    val genNumChar: Gen<Char>
        get() = choose(48, 57).map{n -> n.toChar()}

    /**
     * Generates an uppercase alpha character.
     *
     * genAlphaUpperChar:: Gen[Char]
     */
    val genAlphaUpperChar: Gen<Char>
        get() = choose(65, 90).map{n -> n.toChar()}

    /**
     * Generates an lowercase alpha character.
     *
     * genAlphaLowerChar:: Gen[Char]
     */
    val genAlphaLowerChar: Gen<Char>
        get() = choose(97, 122).map{n -> n.toChar()}

    /**
     * Generates an alpha character.
     *
     * genAlphaChar:: Gen[Char]
     */
    val genAlphaChar: Gen<Char>
        get() = frequency(ListF.from(Pair(1, genAlphaUpperChar), Pair(9, genAlphaLowerChar)))

    /**
     * Generates an alphanumeric character.
     *
     * genAlphaNumChar:: Gen[Char]
     */
    val genAlphaNumChar: Gen<Char>
        get() = frequency(ListF.from(Pair(1, genNumChar), Pair(9, genAlphaChar)))



// ---------- string generators ---------------------------

    /**
     * Generate a string that starts with a lowercase alpha character,
     *   and only contains alphanumeric characters.
     *
     * genIdentifier:: Gen[String]
     */
    val genIdentifier: Gen<String>
        get() = genAlphaLowerChar.bind{c: Char ->
            genList(genAlphaNumChar).bind{cs: List<Char> ->
                val string: String = cs.join()
                Gen{_: Int ->
                    {_: Rand ->
                        "$c$string"
                    }
                }
            }
        }   // genIdentifier

    /**
     * Generate an identifier no greater than given size.
     */
    fun genIdentifier(max: Int): Gen<String> =
        genStr(max, genAlphaLowerChar, genAlphaNumChar)

    /**
     * Generate a string of alpha characters.
     *
     * genAlphaStr:: Gen[String]
     */
    val genAlphaStr: Gen<String>
        get() = genList(genAlphaChar).bind{cs: List<Char> ->
            val string: String = cs.join()
            Gen{_: Int ->
                {_: Rand ->
                    string
                }
            }
        }

    /**
     * Generate a string of alpha characters no greater than given size.
     */
    fun genAlphaStr(max: Int): Gen<String> =
        genStr(max, genAlphaChar)

    /**
     * Generate string of alpha characters with at least min characters and no greater
     *   then max characters.
     */
    fun genAlphaStr(min: Int, max: Int): Gen<String> =
        genStr(min, max, genAlphaChar)

    /**
     * Generate a string of uppercase alpha characters.
     *
     * genAlphaUpperStr:: Gen[String]
     */
    val genAlphaUpperStr: Gen<String>
        get() = genList(genAlphaUpperChar).bind{cs: List<Char> ->
            val string: String = cs.join()
            Gen{_: Int ->
                {_: Rand ->
                    string
                }
            }
        }

    /**
     * Generate a string of uppercase alpha characters no greater than given size.
     */
    fun genAlphaUpperStr(max: Int): Gen<String> =
        genStr(max, genAlphaUpperChar)

    /**
     * Generate a string of uppercase alpha characters with at least min characters
     *  and no greater than max size.
     */
    fun genAlphaUpperStr(min: Int, max: Int): Gen<String> =
        genStr(min, max, genAlphaUpperChar)

    /**
     * Generate a string of lowercase alpha characters.
     *
     * genAlphaLowerStr:: Gen[String]
     */
    val genAlphaLowerStr: Gen<String>
        get() = genList(genAlphaLowerChar).bind{cs: List<Char> ->
            val string: String = cs.join()
            Gen{_: Int ->
                {_: Rand ->
                    string
                }
            }
        }

    /**
     * Generate a string of lowercase alpha characters no greater than given size.
     */
    fun genAlphaLowerStr(max: Int): Gen<String> =
        genStr(max, genAlphaLowerChar)

    /**
     * Generate a string of lowercase alpha characters with at least min characters
     *   and no greater than max size.
     */
    fun genAlphaLowerStr(min: Int, max: Int): Gen<String> =
        genStr(min, max, genAlphaLowerChar)

    /**
     * Generate a string of alpha-numeric characters.
     *
     * genAlphaNumStr:: Gen[String]
     */
    val genAlphaNumStr: Gen<String>
        get() = genList(genAlphaNumChar).bind{cs: List<Char> ->
            val string: String = cs.join()
            Gen{_: Int ->
                {_: Rand ->
                    string
                }
            }
        }

    /**
     * Generate a string alpha-numeric characters no greater than given size.
     */
    fun genAlphaNumStr(max: Int): Gen<String> =
        genStr(max, genAlphaNumChar)

    /**
     * Generate a string alpha-numeric characters with at least min characters
     *   and no greater than max size.
     */
    fun genAlphaNumStr(min: Int, max: Int): Gen<String> =
        genStr(min, max, genAlphaNumChar)

    /**
     * Generate a string of digits.
     *
     * genNumStr:: Gen[String]
     */
    val genNumStr: Gen<String>
        get() = genList(genNumChar).bind{cs: List<Char> ->
            val string: String = cs.join()
            Gen{_: Int ->
                {_: Rand ->
                    string
                }
            }
        }

    /**
     * Generate a string of digits no greater than given size.
     */
    fun genNumStr(max: Int): Gen<String> =
        genStr(max, genNumChar)

    /**
     * Generate a string of digits with at least min characters and no greater
     *   than max size.
     */
    fun genNumStr(min: Int, max: Int): Gen<String> =
        genStr(min, max, genNumChar)

    /**
     * Generate a string.
     *
     * genString:: Gen[String]
     */
    val genString: Gen<String>
        get() = genList(choose(32, 126).map{n: Int -> n.toChar()}).bind{cs: List<Char> ->
            val string: String = cs.join()
            Gen{_: Int ->
                {_: Rand ->
                    string
                }
            }
        }

    /**
     * Generate a string no greater than given size.
     */
    fun genString(max: Int): Gen<String> =
        genStr(max, choose(32, 126).map{n: Int -> n.toChar()})

    /**
     * Generate a string with at least min characters and no greater than max size.
     */
    fun genString(min: Int, max: Int): Gen<String> =
        genStr(min, max, choose(32, 126).map{n: Int -> n.toChar()})



// ---------- calendar generators -------------------------

    val genCalendar: Gen<Calendar>
        get() = choose(-62135751600000L, 64087186649116L).map {millis ->
            val calendar: Calendar = Calendar.getInstance()
            calendar.timeInMillis = millis
            calendar
        }   // genCalendar



// ---------- other generators ----------------------------

    /**
     * An implementation for pairs.
     *
     * @param genA              generator for the first of the pair
     * @param genB              generator for the second of the pair
     * @return                  generator for pairs
     */
    fun <A, B> genPair(genA: Gen<A>, genB: Gen<B>): Gen<Pair<A, B>> =
        genA.bind{a ->
            genB.bind{b ->
                value(Pair(a, b))
            }
        }   // genPair

    /**
     * An implementation over enumerations.
     */
    fun <A : Enum<A>> genEnum(klass: Class<A>): Gen<A> =
        elements(*(klass.getEnumConstants() as Array<A>))

    /**
     * An implementation for arrays.
     *
     * @param genA              generator for the array elements
     * @return                  generator for arrays
     */
    inline fun <reified A> genArray(genA: Gen<A>): Gen<Array<A>> {
        val listGen: Gen<List<A>> = genList1(genA)
        return listGen.map{ls -> ListF.toList(ls).toTypedArray()}
    }

    /**
     * Returns an implementation for optional values.
     *
     * @param genA              generator for the type over which the optional value is defined
     * @return                  generator for optional values
     */
    fun <A> genOption(genA: Gen<A>): Gen<Option<A>> {
        val genNone: Gen<Option<A>> = value(None)
        return frequency(
            ListF.of(
                Pair(1, genNone),
                Pair(4, genA.bind{a: A ->
                    val some: Gen<Option<A>> = value(some(a))
                    some
                })
            )
        )
    }   // genOption

    /**
     * Returns an implementation for identity values.
     *
     * @param genA              generator for the type over which the identity value is defined
     * @return                  generator for identity values
     */
    fun <A> genIdentity(genA: Gen<A>): Gen<Identity<A>> =
            genA.bind{a: A ->
                value(identity(a))
            }

    /**
     * Returns an implementation for the disjoint union.
     *
     * @param genA                  generator for the left side of the disjoint union
     * @param genB                  generator for the right side of the disjoint union
     * @return                      generator for the disjoint union
     */
    fun <A, B> genEither(genA: Gen<A>, genB: Gen<B>): Gen<Either<A, B>> {
        val genLeft: Gen<Either<A, B>> = genA.bind{a: A ->
            val left: Gen<Either<A, B>> = value(left(a))
            left
        }
        val genRight: Gen<Either<A, B>> = genB.bind{b: B ->
            val right: Gen<Either<A, B>> = value(right(b))
            right
        }
        return oneOf(ListF.of(genLeft, genRight))
    }   // genEither

    /**
     * Return a generator of lists using the given generator for the values.
     *
     * @param n                 minimum size of the generated list
     * @param genA              the generator for the individual elements
     * @return                  a generator of lists of size n whose values come from the given generator
     */
    fun <A> genList(n: Int, genA: Gen<A>): Gen<List<A>> =
        sized{size -> choose(n, max(n, size)).bind{m -> sequence(m, genA)}}

    /**
     * Return a generator of (possibly empty) lists using the given generator for the values.
     *
     * @param genA              the generator for the individual elements
     * @return                  a generator of (possibly empty) lists whose values come from the given generator
     */
    fun <A> genList(genA: Gen<A>): Gen<List<A>> = genList(0, genA)

    /**
     * Return a generator of lists of at least size 1 using the given generator for the values.
     *
     * @param genA              the generator for the individual elements
     * @return                  a generator of lists of at least size 1 whose values come from the given generator
     */
    fun <A> genList1(genA: Gen<A>): Gen<List<A>> = genList(1, genA)

    /**
     * A generator of lists of booleans.
     */
    val genListBoolean: Gen<List<Boolean>>
        get() = genList(genBoolean)

    /**
     * A generator of lists of integers.
     */
    val genListInt: Gen<List<Int>>
        get() = genList(genInt)

    /**
     * A generator of lists of doubles.
     */
    val genListDouble: Gen<List<Double>>
        get() = genList(genDouble)

    /**
     * A generator of lists of strings.
     */
    val genListString: Gen<List<String>>
        get() = genList(genString)

    /**
     * Return a generator of lists using the given generator for the values.
     *
     * @param n                 size to apply to the given generator
     * @param genA              the generator for the individual elements
     * @return                  a generator of lists whose values come from the given generator
     */
    fun <A> genKList(n: Int, genA: Gen<A>): Gen<KList<A>> =
        sized{size -> choose(n, max(n, size)).bind{m -> sequenceK(m, genA)}}

    /**
     * Return a generator of lists using the given generator for the values.
     *
     * @param genA              the generator for the individual elements
     * @return                  a generator of lists whose values come from the given generator
     */
    fun <A> genKList(genA: Gen<A>): Gen<KList<A>> = genKList(0, genA)

    /**
     * Return a generator of lists using the given generator for the values.
     *
     * @param genA              the generator for the individual elements
     * @return                  a generator of lists whose values come from the given generator
     */
    fun <A> genKList1(genA: Gen<A>): Gen<KList<A>> = genKList(1, genA)

    /**
     * Return a generator of maps using the given generators for the keys and values.
     *
     * @param genK              the generator for the individual keys
     * @param genV              the generator for the individual values
     * @return                  a generator of maps
     */
    fun <K : Comparable<K>, V> genMap(genK: Gen<K>, genV: Gen<V>): Gen<Map<K, V>> {
        fun arbMap(n: Int): Gen<Map<K, V>> {
            return if (n == 0)
                value(MapF.empty())
            else
                frequency(ListF.of(
                    Pair(1, value(MapF.empty())),
                    Pair(4, genK.bind{key: K ->
                        genV.bind{value: V ->
                            arbMap(n / 2).bind{left: Map<K, V> ->
                                arbMap(n / 2).bind{right: Map<K, V> ->
                                    value(left.union(right).insert(key, value))
                                }
                            }
                        }
                    })
                ))
        }   // arbMap

        return sized(::arbMap)
    }   // genMap

    /**
     * Return a generator of HAMT maps using the given generators for the keys and values.
     *
     * @param genK              the generator for the individual keys
     * @param genV              the generator for the individual values
     * @return                  a generator of HAMT maps
     */
    fun <K : Comparable<K>, V> genHAMTMap(genK: Gen<K>, genV: Gen<V>): Gen<HAMTMap<K, V>> {
        // HOPELESSLY inefficient: need an efficient union implementation for HAMT
        fun union(map1: HAMTMap<K, V>, map2: HAMTMap<K, V>): HAMTMap<K, V> {
            fun listUnion(list1: List<Pair<K, V>>, list2: List<Pair<K, V>>): List<Pair<K, V>> {
                tailrec
                fun recListUnion(list1: List<Pair<K, V>>, list2: List<Pair<K, V>>, acc: List<Pair<K, V>>): List<Pair<K, V>> {
                    return if (list2.isEmpty())
                        list1.append(acc)
                    else when (list1) {
                        is Nil -> acc.append(list2)
                        is Cons -> {
                            val head1: Pair<K, V> = list1.head()
                            val head2: Pair<K, V> = list2.head()
                            if (head1.first < head2.first)
                                recListUnion(list1.tail(), list2, ListF.cons(head1, acc))
                            else if (head1.first > head2.first)
                                recListUnion(list1, list2.tail(), ListF.cons(head2, acc))
                            else
                                recListUnion(list1.tail(), list2.tail(), ListF.cons(head2, acc))
                        }
                    }
                }   // recListUnion

                return recListUnion(list1, list2, ListF.empty())
            }   // listUnion

            val list1: List<Pair<K, V>> = map1.toAscendingList()
            val list2: List<Pair<K, V>> = map2.toAscendingList()

            return HAMTMapF.from(listUnion(list1, list2))
        }   // union

        fun arbHAMTMap(n: Int): Gen<HAMTMap<K, V>> {
            return if (n == 0)
                value(HAMTMapF.empty())
            else
                frequency(ListF.of(
                        Pair(1, value(HAMTMapF.empty())),
                        Pair(4, genK.bind{key: K ->
                            genV.bind{value: V ->
                                arbHAMTMap(n / 2).bind{left: HAMTMap<K, V> ->
                                    arbHAMTMap(n / 2).bind{right: HAMTMap<K, V> ->
                                        value(union(left, right).insert(key, value))
                                    }
                                }
                            }
                        })
                ))
        }   // arbMap

        return sized(::arbHAMTMap)
    }   // genHAMTMap

    /**
     * Return a generator for non empty lists given the generator for the elements.
     *
     * @param genA              the generator for the individual elements
     * @return                  a generator for non empty lists
     */
    fun <A> genNonEmptyList(genA: Gen<A>): Gen<NonEmptyList<A>> {
        val genList: Gen<List<A>> = genList(genA)
        return genA.bind{a: A ->
            genList.bind{list ->
                value(nonEmptyList(a, list))
            }
        }
    }   // genNonEmptyList

    /**
     * Return a generator for a stream given the generator for the elements.
     *
     * @param genA              the generator for the individual elements
     * @return                  a generator for a stream
     */
    fun <A> genStream(genA: Gen<A>): Gen<Stream<A>> =
        genList(genA).map{list -> StreamF.from(list)}

    /**
     * Return a generator for sets given the generator for the elements.
     *
     * @param genA              the generator for the individual elements
     * @return                  a generator for sets
     */
    fun <A : Comparable<A>> genSet(genA: Gen<A>): Gen<Set<A>> {
        fun arbSet(n: Int): Gen<Set<A>> {
            return if (n == 0)
                value(SetF.empty())
            else
                frequency(ListF.of(
                    Pair(1, value(SetF.empty())),
                    Pair(4, genA.bind{a: A ->
                        arbSet(n / 2).bind{left: Set<A> ->
                            arbSet(n / 2).bind{right: Set<A> ->
                                value(left.union(right).insert(a))
                            }
                        }
                    })
                ))
        }   // arbSet

        return sized(::arbSet)
    }   // genSet

    /**
     * Return a generator for trees given the generator for the elements.
     *
     * @param genA              the generator for the individual elements
     * @return                  a generator for trees
     */
    fun <A : Comparable<A>> genTree(genA: Gen<A>): Gen<Tree<A>> {
        fun arbTree(n: Int): Gen<Tree<A>> {
            return if (n == 0)
                value(TreeF.empty())
            else
                frequency(ListF.of(
                    Pair(1, value(TreeF.empty())),
                    Pair(4, genA.bind{a: A ->
                        arbTree(n / 2).bind{left: Tree<A> ->
                            arbTree(n / 2).bind{right: Tree<A> ->
                                value(left.union(right).insert(a))
                            }
                        }
                    })
                ))
        }   // arbTree

        return sized(::arbTree)
    }   // genTree

    /**
     * Return a generator for red-black-trees given the generators for the elements.
     *
     * @param genK              the generator for the key elements
     * @param genV              the generator for the value elements
     * @return                  a generator for red-black-trees
     */
    fun <K : Comparable<K>, V> genRBTree(genK: Gen<K>, genV: Gen<V>): Gen<RedBlackTree<K, V>> {
        fun arbTree(n: Int): Gen<RedBlackTree<K, V>> {
            return if (n == 0)
                value<RedBlackTree<K, V>>(RedBlackTree.EmptyTree())
            else
                oneOf(
                        value<RedBlackTree<K, V>>(RedBlackTree.EmptyTree()),
                        genK.bind{key ->
                            genV.bind{value ->
                                arbTree(n / 2).bind{left ->
                                    arbTree(n / 2).bind{right ->
                                        value<RedBlackTree<K, V>>(RedBlackTree.RBTree.RedTree(key, value, left, right))
                                    }
                                }
                            }
                        },
                        genK.bind{key ->
                            genV.bind{value ->
                                arbTree(n / 2).bind{left ->
                                    arbTree(n / 2).bind{right ->
                                        value<RedBlackTree<K, V>>(RedBlackTree.RBTree.BlackTree(key, value, left, right))
                                    }
                                }
                            }
                        }
                )
        }

        return sized(::arbTree)
    }   // genRBTree

    /**
     * Return a generator for the class Try.
     *
     * @param genStr            generator for failure values
     * @param genA              generator for success values
     * @return                  generator for the class Try
     */
    fun <A> genTry(genA: Gen<A>): Gen<Try<A>> {
        val genFailure: Gen<Try<A>> = genString.bind{str: String ->
            val failure: Gen<Try<A>> = value(TryF.failure(str))
            failure
        }
        val genSuccess: Gen<Try<A>> = genA.bind{a: A ->
            val success: Gen<Try<A>> = value(TryF.success(a))
            success
        }
        return oneOf(ListF.of(genFailure, genSuccess))
    }   // genTry

    /**
     * Return a generator for the class Validation.
     *
     * @param genE              generator for failure values
     * @param genA              generator for success values
     * @return                  generator for the class Validation
     */
    fun <E, A> genValidation(genE: Gen<E>, genA: Gen<A>): Gen<Validation<E, A>> {
        val genFailure: Gen<Validation<E, A>> = genE.bind{e: E ->
            val failure: Gen<Validation<E, A>> = value(ValidationF.failure(e))
            failure
        }
        val genSuccess: Gen<Validation<E, A>> = genA.bind{a: A ->
            val success: Gen<Validation<E, A>> = value(ValidationF.success(a))
            success
        }
        return oneOf(ListF.of(genFailure, genSuccess))
    }   // genValidation



// ---------- categorical functions -----------------------

    // Monad:

    fun <A, B> liftM(f: (A) -> B): (Gen<A>) -> Gen<B> =
        {genA: Gen<A> ->
            genA.bind{a: A ->
                value(f(a))
            }
        }   // liftM

    fun <A, B, C> liftM2(f: (A) -> (B) -> C): (Gen<A>) -> (Gen<B>) -> Gen<C> =
        {genA: Gen<A> ->
            {genB: Gen<B> ->
                genA.bind{a: A ->
                    genB.bind{b: B ->
                        value(f(a)(b))
                    }
                }
            }
        }   // liftM2

    // etc



// ---------- implementation ------------------------------

    /**
     * Extension function on a list of characters to convert into a string.
     */
    private fun List<Char>.join(): String = this.foldLeft(""){res -> {ch -> res + ch.toString()}}

    /**
     * Generate a string no greater than given size.
     */
    private fun genStr(max: Int, gen: Gen<Char>): Gen<String> {
        return genList(gen).bind{cs: List<Char> ->
            val css: List<Char> = cs.take(max)
            val string: String = css.join()
            Gen{_: Int ->
                {_: Rand ->
                    string
                }
            }
        }
    }   // genStr

    /**
     * Generate  string with at least min characters and no greater
     *   then max characters.
     */
    private fun genStr(min: Int, max: Int, gen: Gen<Char>): Gen<String> {
        return gen.bind{c: Char ->
            genList(min - 1, gen).bind{cs: List<Char> ->
                val css: List<Char> = cs.take(max - 1)
                val string: String = css.join()
                Gen{_: Int ->
                    {_: Rand ->
                        "$c$string"
                    }
                }
            }
        }
    }   // genStr

    /**
     * Generate a string no greater than given size.
     */
    private fun genStr(max: Int, genFirst: Gen<Char>, genRemainder: Gen<Char>): Gen<String> {
        return genFirst.bind{c: Char ->
            genList(genRemainder).bind{cs: List<Char> ->
                val css: List<Char> = cs.take(max - 1)
                val string: String = css.join()
                Gen{_: Int ->
                    {_: Rand ->
                        "$c$string"
                    }
                }
            }
        }
    }   // genStr

}   // GenF

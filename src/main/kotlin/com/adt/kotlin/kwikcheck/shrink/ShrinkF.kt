package com.adt.kotlin.kwikcheck.shrink

/**
 * Represents a shrinking strategy over the given type parameter if that type can be represented as
 *   a tree structure. This is used in falsification to produce the smallest counter-example, rather
 *   than the first counter-example.
 *
 * @author	                    Ken Barclay
 * @since                       April 2020
 */

import com.adt.kotlin.kats.data.immutable.either.Either
import com.adt.kotlin.kats.data.immutable.either.Either.Left
import com.adt.kotlin.kats.data.immutable.either.Either.Right
import com.adt.kotlin.kats.data.immutable.either.EitherF.left
import com.adt.kotlin.kats.data.immutable.either.EitherF.right
import com.adt.kotlin.kats.data.immutable.identity.Identity
import com.adt.kotlin.kats.data.immutable.identity.IdentityF.identity

import com.adt.kotlin.kats.data.immutable.list.append
import com.adt.kotlin.kats.data.immutable.list.interleave
import com.adt.kotlin.kats.data.immutable.list.List
import com.adt.kotlin.kats.data.immutable.list.List.Nil
import com.adt.kotlin.kats.data.immutable.list.List.Cons
import com.adt.kotlin.kats.data.immutable.list.ListF

import com.adt.kotlin.kats.data.immutable.option.Option
import com.adt.kotlin.kats.data.immutable.option.Option.None
import com.adt.kotlin.kats.data.immutable.option.Option.Some
import com.adt.kotlin.kats.data.immutable.option.OptionF.none
import com.adt.kotlin.kats.data.immutable.option.OptionF.some

import com.adt.kotlin.kats.data.immutable.map.Map
import com.adt.kotlin.kats.data.immutable.map.MapF
import com.adt.kotlin.kats.data.immutable.nel.NonEmptyList
import com.adt.kotlin.kats.data.immutable.nel.NonEmptyListF
import com.adt.kotlin.kats.data.immutable.nel.NonEmptyListF.nonEmptyList
import com.adt.kotlin.kats.data.immutable.set.Set
import com.adt.kotlin.kats.data.immutable.set.SetF

import com.adt.kotlin.kats.data.immutable.stream.Stream
import com.adt.kotlin.kats.data.immutable.stream.StreamF

import com.adt.kotlin.kats.data.immutable.tree.Tree
import com.adt.kotlin.kats.data.immutable.tree.TreeF

import com.adt.kotlin.kats.data.immutable.validation.Validation
import com.adt.kotlin.kats.data.immutable.validation.Validation.Failure
import com.adt.kotlin.kats.data.immutable.validation.Validation.Success
import com.adt.kotlin.kats.data.immutable.validation.ValidationF.failure
import com.adt.kotlin.kats.data.immutable.validation.ValidationF.success



object ShrinkF {

    /**
     * Functions that convert between primitive types.
     */
    val booleanToByte: (Boolean) -> Byte = {b: Boolean -> (if (b) 1 else 0).toByte()}
    val booleanToChar: (Boolean) -> Char = {b: Boolean -> (if (b) 1 else 0).toChar()}
    val booleanToDouble: (Boolean) -> Double = {b: Boolean -> (if (b) 1 else 0).toDouble()}
    val booleanToFloat: (Boolean) -> Float = {b: Boolean -> (if (b) 1 else 0).toFloat()}
    val booleanToInt: (Boolean) -> Int = {b: Boolean -> (if (b) 1 else 0).toInt()}
    val booleanToLong: (Boolean) -> Long = {b: Boolean -> (if (b) 1 else 0).toLong()}
    val booleanToShort: (Boolean) -> Short = {b: Boolean -> (if (b) 1 else 0).toShort()}

    val byteToBoolean: (Byte) -> Boolean = {b: Byte -> (b != 0.toByte())}
    val byteToChar: (Byte) -> Char = {b: Byte -> b.toChar()}
    val byteToDouble: (Byte) -> Double = {b: Byte -> b.toDouble()}
    val byteToFloat: (Byte) -> Float = {b: Byte -> b.toFloat()}
    val byteToInt: (Byte) -> Int = {b: Byte -> b.toInt()}
    val byteToLong: (Byte) -> Long = {b: Byte -> b.toLong()}
    val byteToShort: (Byte) -> Short = {b: Byte -> b.toShort()}

    val charToBoolean: (Char) -> Boolean = {c: Char -> (c.toInt() != 0)}
    val charToByte: (Char) -> Byte = {c: Char -> c.toByte()}
    val charToDouble: (Char) -> Double = {c: Char -> c.toDouble()}
    val charToFloat: (Char) -> Float = {c: Char -> c.toFloat()}
    val charToInt: (Char) -> Int = {c: Char -> c.toInt()}
    val charToLong: (Char) -> Long = {c: Char -> c.toLong()}
    val charToShort: (Char) -> Short = {c: Char -> c.toShort()}

    val doubleToBoolean: (Double) -> Boolean = {d: Double -> (d != 0.toDouble())}
    val doubleToByte: (Double) -> Byte = {d: Double -> d.toInt().toByte()}
    val doubleToChar: (Double) -> Char = {d: Double -> d.toChar()}
    val doubleToFloat: (Double) -> Float = {d: Double -> d.toFloat()}
    val doubleToInt: (Double) -> Int = {d: Double -> d.toInt()}
    val doubleToLong: (Double) -> Long = {d: Double -> d.toLong()}
    val doubleToShort: (Double) -> Short = {d: Double -> d.toInt().toShort()}

    val floatToBoolean: (Float) -> Boolean = {f: Float -> (f != 0.toFloat())}
    val floatToByte: (Float) -> Byte = {f: Float -> f.toInt().toByte()}
    val floatToChar: (Float) -> Char = {f: Float -> f.toChar()}
    val floatToDouble: (Float) -> Double = {f: Float -> f.toDouble()}
    val floatToInt: (Float) -> Int = {f: Float -> f.toInt()}
    val floatToLong: (Float) -> Long = {f: Float -> f.toLong()}
    val floatToShort: (Float) -> Short = {f: Float -> f.toInt().toShort()}

    val intToBoolean: (Int) -> Boolean = {n: Int -> (n != 0)}
    val intToByte: (Int) -> Byte = {n: Int -> n.toByte()}
    val intToChar: (Int) -> Char = {n: Int -> n.toChar()}
    val intToDouble: (Int) -> Double = {n: Int -> n.toDouble()}
    val intToFloat: (Int) -> Float = {n: Int -> n.toFloat()}
    val intToLong: (Int) -> Long = {n: Int -> n.toLong()}
    val intToShort: (Int) -> Short = {n: Int -> n.toShort()}

    val longToBoolean: (Long) -> Boolean = {l: Long -> (l != 0.toLong())}
    val longToByte: (Long) -> Byte = {l: Long -> l.toByte()}
    val longToChar: (Long) -> Char = {l: Long -> l.toChar()}
    val longToDouble: (Long) -> Double = {l: Long -> l.toDouble()}
    val longToFloat: (Long) -> Float = {l: Long -> l.toFloat()}
    val longToInt: (Long) -> Int = {l: Long -> l.toInt()}
    val longToShort: (Long) -> Short = {l: Long -> l.toShort()}

    val shortToBoolean: (Short) -> Boolean = {s: Short -> (s != 0.toShort())}
    val shortToByte: (Short) -> Byte = {s: Short -> s.toByte()}
    val shortToChar: (Short) -> Char = {s: Short -> s.toChar()}
    val shortToDouble: (Short) -> Double = {s: Short -> s.toDouble()}
    val shortToFloat: (Short) -> Float = {s: Short -> s.toFloat()}
    val shortToInt: (Short) -> Int = {s: Short -> s.toInt()}
    val shortToLong: (Short) -> Long = {s: Short -> s.toLong()}



    /**
     * Returns a shrink strategy that cannot be reduced further.
     *
     * @return                      a shrink strategy that cannot be reduced further
     */
    fun <A> empty(): Shrink<A> =
        Shrink{_: A -> ListF.empty()}

    /**
     * Factory constructor.
     */
    fun <T> shrink(func: ShrinkFunc<T>): Shrink<T> = Shrink(func)

    /**
     * A shrink strategy for longs using 0 as the bottom of the shrink.
     */
    val shrinkLong: Shrink<Long>
        get() = Shrink{lng: Long ->
            if (lng == 0L)
                ListF.empty()
            else {
                fun iterate(start: Long, f: (Long) -> Long, predicate: (Long) -> Boolean): List<Long> {
                    fun recIterate(start: Long, f: (Long) -> Long, predicate: (Long) -> Boolean, acc: List<Long>): List<Long> {
                        return if (predicate(start))
                            acc
                        else
                            recIterate(f(start), f, predicate, acc.append(start))
                    }   // recIterate

                    return recIterate(start, f, predicate, ListF.empty())
                }   // iterate

                val ls: List<Long> = ListF.cons(0L, iterate(lng, {n -> n / 2L}){n -> (n != 0L)}.map{n -> lng - n})
                if (lng < 0L) ListF.cons(-lng, ls) else ls
            }
        }   // shrinkLong

    /**
     * A shrink strategy for booleans using false as the bottom of the shrink.
     */
    val shrinkBoolean: Shrink<Boolean>
        get() = shrinkLong.map2(longToBoolean, booleanToLong)

    /**
     * A shrink strategy for bytes using 0 as the bottom of the shrink.
     */
    val shrinkByte: Shrink<Byte>
        get() = shrinkLong.map2(longToByte, byteToLong)

    /**
     * A shrink strategy for doubles using 0 as the bottom of the shrink.
     */
    val shrinkDouble: Shrink<Double>
        get() = shrinkLong.map2(longToDouble, doubleToLong)

    /**
     * A shrink strategy for floats using 0 as the bottom of the shrink.
     */
    val shrinkFloat: Shrink<Float>
        get() = shrinkLong.map2(longToFloat, floatToLong)

    /**
     * A shrink strategy for integers using 0 as the bottom of the shrink.
     */
    val shrinkInt: Shrink<Int>
        get() = shrinkLong.map2(longToInt, intToLong)

    /**
     * A shrink strategy for shorts using 0 as the bottom of the shrink.
     */
    val shrinkShort: Shrink<Short>
        get() = shrinkLong.map2(longToShort, shortToLong)

    /**
     * A shrink strategy for characters using 0 as the bottom of the shrink.
     */
    val shrinkChar: Shrink<Char>
        get() = shrinkLong.map2(longToChar, charToLong)

    /**
     * A shrink strategy for strings using the empty string as the bottom of the shrink.
     */
    val shrinkString: Shrink<String>
        get() {
            val listToString: (List<Char>) -> String = {list -> list.foldLeft(""){str -> {ch -> "$str$ch"}}}
            val stringToList: (String) -> List<Char> = {str -> ListF.from(str)}
            return shrinkList(shrinkChar).map2(listToString, stringToList)
        }   // shrinkString


    /**
     * Return a shrink strategy for the standard Kotlin Pair.
     */
    fun <A, B> shrinkPair(sa: Shrink<A>, sb: Shrink<B>): Shrink<Pair<A, B>> =
        shrink{pair: Pair<A, B> ->
            sa.shrink(pair.first).map{a: A -> Pair(a, pair.second)}.append(sb.shrink(pair.second).map{b: B -> Pair(pair.first, b)})
        }   // shrinkPair

    /**
     * Return a shrink strategy for immutable Identity values. The shrinking
     *   occurs on the value with the given shrink strategy.
     *
     * @param sa                the shrink strategy for the potential value
     * @return                  a shrink strategy for identity values
     */
    fun <A> shrinkIdentity(sa: Shrink<A>): Shrink<Identity<A>> =
            shrink{id: Identity<A> ->
                sa.shrink(id.value).map{a: A -> identity(a)}
            }

    /**
     * Return a shrink strategy for immutable Option values. A 'no value' is already fully
     *   shrunk, otherwise, the shrinking occurs on the value with the given shrink strategy.
     *
     * @param sa                the shrink strategy for the potential value
     * @return                  a shrink strategy for optional values
     */
    fun <A> shrinkOption(sa: Shrink<A>): Shrink<Option<A>> =
        shrink{op: Option<A> ->
            when (op) {
                is None -> ListF.empty()
                is Some -> ListF.cons(none(), sa.shrink(op.value).map{a -> some(a)})
            }
        }   // shrinkOption

    /**
     * Return a shrink strategy for immutable Either values.
     *
     * @param sa                the shrinking strategy for left values
     * @param sb                the shrinking strategy for right values
     * @return                  a shrink strategy for either values
     */
    fun <A, B> shrinkEither(sa: Shrink<A>, sb: Shrink<B>): Shrink<Either<A, B>> =
        shrink{ei: Either<A, B> ->
            when (ei) {
                is Left -> sa.shrink(ei.value).map{a -> left(a)}
                is Right -> sb.shrink(ei.value).map{b -> right(b)}
            }
        }   // shrinkEither

    /**
     * Return a shrink strategy for immutable Lists. An empty list is fully shrunk.
     *
     * @param sa                the shrink strategy for the elements of the list
     * @return                  a shrink strategy for lists
     */
    fun <A> shrinkList(sa: Shrink<A>): Shrink<List<A>> {
        fun removeChunks(n: Int, xs: List<A>): List<List<A>> {
            return when (xs) {
                is Nil -> ListF.empty()
                is Cons -> {
                    when (xs.tail()) {
                        is Nil -> ListF.empty()
                        is Cons -> {
                            val n1: Int = n / 2
                            val n2: Int = n - n1

                            val xs1: List<A> = xs.take(n1)
                            val xs2: List<A> = xs.drop(n1)

                            val ss3: List<List<A>> = removeChunks(n1, xs1).filter{ys -> !ys.isEmpty()}.map{ys -> ys.append(xs2)}
                            val ss4: List<List<A>> = removeChunks(n2, xs2).filter{ys -> !ys.isEmpty()}.map{ys -> xs1.append(ys)}

                            ListF.cons(xs1, ListF.cons(xs2, ss3.interleave(ss4)))
                        }
                    }
                }
            }
        }   // removeChunks

        fun shrinkOne(xs: List<A>): List<List<A>> {
            return when (xs) {
                is Nil -> ListF.empty()
                is Cons -> {
                    val consHeadF: (List<A>) -> List<A> = {ys -> ListF.cons(xs.head(), ys)}
                    val consF: (A) -> List<A> = {a -> ListF.cons(a, xs.tail())}
                    sa.shrink(xs.head()).map(consF).append(shrinkOne(xs.tail()).map(consHeadF))
                }
            }
        }   // shrinkOne

        return shrink{xs -> removeChunks(xs.size(), xs).append(shrinkOne(xs))}
    }   // shrinkList

    /**
     * Returns a shrink strategy for immutable Map values.
     *
     * @param sa                the shrinking strategy for the keys
     * @param sb                the shrinking strategy for the values
     * @return                  a shrink strategy for Map values
     */
    fun <A: Comparable<A>, B> shrinkMap(sa: Shrink<A>, sb: Shrink<B>): Shrink<Map<A, B>> =
        shrinkList(shrinkPair(sa, sb)).map2(
            {list: List<Pair<A, B>> -> MapF.fromList(list)},
            {map: Map<A, B> -> map.toAscendingList()}
        )   // shrinkMap

    /**
     * Return a shrink strategy for immutable NonEmptyLists.
     *
     * @param sa                the shrink strategy for the elements of the list
     * @param a                 the shrink value for an empty list
     * @return                  a shrink strategy for lists
     */
    fun <A> shrinkNonEmptyList(sa: Shrink<A>, a: A): Shrink<NonEmptyList<A>> =
        shrinkList(sa).map2(
            {list: List<A> -> if (list.isEmpty()) NonEmptyListF.singleton(a) else nonEmptyList(list.head(), list.tail())},
            {nel: NonEmptyList<A> -> nel.toList()}
        )   // shrinkNonEmptyList

    /**
     * Return a shrink strategy for immutable Streams.
     *
     * @param sa                the shrink strategy for the elements of the stream
     * @return                  a shrink strategy for streams
     */
    fun <A> shrinkStream(sa: Shrink<A>): Shrink<Stream<A>> =
        shrinkList(sa).map2(
            {list: List<A> -> StreamF.from(list)},
            {stream: Stream<A> -> stream.toList()}
        )   // shrinkStream

    /**
     * Return a shrink strategy for immutable Sets.
     *
     * @param sa                the shrink strategy for the elements of the set
     * @return                  a shrink strategy for sets
     */
    fun <A : Comparable<A>> shrinkSet(sa: Shrink<A>): Shrink<Set<A>> =
        shrinkList(sa).map2(
            {list: List<A> -> SetF.fromList(list)},
            {set: Set<A> -> set.toAscendingList()}
        )   // shrinkSet

    /**
     * Return a shrink strategy for immutable Trees.
     *
     * @param sa                the shrink strategy for the elements of the tre
     * @return                  a shrink strategy for trees
     */
    fun <A : Comparable<A>> shrinkTree(sa: Shrink<A>): Shrink<Tree<A>> =
        shrinkList(sa).map2(
            {list: List<A> -> TreeF.fromList(list)},
            {tree: Tree<A> -> tree.toAscendingList()}
        )   // shrinkTree

    /**
     * Return a shrink strategy for immutable Validation values.
     *
     * @param sa                the shrinking strategy for failure values
     * @param sb                the shrinking strategy for success values
     * @return                  a shrink strategy for validation values
     */
    fun <A, B> shrinkValidation(sa: Shrink<A>, sb: Shrink<B>): Shrink<Validation<A, B>> =
        shrink{va: Validation<A, B> ->
            when (va) {
                is Failure -> sa.shrink(va.err).map{ a -> failure(a)}
                is Success -> sb.shrink(va.value).map{ b -> success(b)}
            }
        }   // shrinkValidation

}   // ShrinkF

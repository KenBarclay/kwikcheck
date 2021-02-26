package com.adt.kotlin.kwikcheck.generator

/**
 * Transform a type and generator to produce a new generator.
 *
 * @author	                    Ken Barclay
 * @since                       April 2020
 */

import com.adt.kotlin.kats.data.immutable.either.Either
import com.adt.kotlin.kats.data.immutable.either.Either.Left
import com.adt.kotlin.kats.data.immutable.either.Either.Right
import com.adt.kotlin.kats.data.immutable.identity.Identity

import com.adt.kotlin.kats.data.immutable.list.List
import com.adt.kotlin.kats.data.immutable.list.List.Nil
import com.adt.kotlin.kats.data.immutable.list.List.Cons
import com.adt.kotlin.kats.data.immutable.list.ListF

import com.adt.kotlin.kats.data.immutable.map.Map
import com.adt.kotlin.kats.data.immutable.map.MapF
import com.adt.kotlin.kats.data.immutable.nel.NonEmptyList

import com.adt.kotlin.kats.data.immutable.option.Option

import com.adt.kotlin.kwikcheck.random.Rand



object CogenF {

    /**
     * A cogen for a function.
     *
     * @param genA              a Gen for the domain of the function
     * @param cogenB            a Cogen for the codomain of the function
     * @return                  a Cogen for a function
     */
    fun <A, B> cogen(genA: Gen<A>, cogenB: Cogen<B>): Cogen<(A) -> B> =
        object: Cogen<(A) -> B>() {
            override fun <X> cogen(value: (A) -> B, gen: Gen<X>): Gen<X> =
                genA.bind{a -> cogenB.cogen(value(a), gen)}
        }   // cogen

    /**
     * A cogen for a binary function.
     *
     * @param genA              a Gen for the domain of the function
     * @param genB              a Gen for the domain of the function
     * @param cogenC            a Cogen for the codomain of the function
     * @return                  a Cogen for a binary function
     */
    fun <A, B, C> cogen(genA: Gen<A>, genB: Gen<B>, cogenC: Cogen<C>): Cogen<(A) -> (B) -> C> =
        object: Cogen<(A) -> (B) -> C>() {
            override fun <X> cogen(value: (A) -> (B) -> C, gen: Gen<X>): Gen<X> =
                cogen(genA, cogen(genB, cogenC)).cogen(value, gen)
        }   // cogen

    /**
     * A cogen for a function of three arguments.
     *
     * @param genA              a Gen for the domain of the function
     * @param genB              a Gen for the domain of the function
     * @param genC              a Gen for the domain of the function
     * @param cogenD            a Cogen for the codomain of the function
     * @return                  a Cogen for a function of three arguments
     */
    fun <A, B, C, D> cogen(genA: Gen<A>, genB: Gen<B>, genC: Gen<C>, cogenD: Cogen<D>): Cogen<(A) -> (B) -> (C) -> D> =
        object: Cogen<(A) -> (B) -> (C) -> D>() {
            override fun <X> cogen(value: (A) -> (B) -> (C) -> D, gen: Gen<X>): Gen<X> =
                cogen(genA, cogen(genB, cogen(genC, cogenD))).cogen(value, gen)
        }   // cogen

    /**
     * A cogen for a function of four arguments.
     *
     * @param genA              a Gen for the domain of the function
     * @param genB              a Gen for the domain of the function
     * @param genC              a Gen for the domain of the function
     * @param genD              a Gen for the domain of the function
     * @param cogenE            a Cogen for the codomain of the function
     * @return                  a Cogen for a function of four arguments
     */
    fun <A, B, C, D, E> cogen(genA: Gen<A>, genB: Gen<B>, genC: Gen<C>, genD: Gen<D>, cogenE: Cogen<E>): Cogen<(A) -> (B) -> (C) -> (D) -> E> =
        object: Cogen<(A) -> (B) -> (C) -> (D) -> E>() {
            override fun <X> cogen(value: (A) -> (B) -> (C) -> (D) -> E, gen: Gen<X>): Gen<X> =
                cogen(genA, cogen(genB, cogen(genC, cogen(genD, cogenE)))).cogen(value, gen)
        }   // cogen

    /**
     * A cogen for a function of five arguments.
     *
     * @param genA              a Gen for the domain of the function
     * @param genB              a Gen for the domain of the function
     * @param genC              a Gen for the domain of the function
     * @param genD              a Gen for the domain of the function
     * @param cogenE            a Cogen for the codomain of the function
     * @return                  a Cogen for a function of five arguments
     */
    fun <A, B, C, D, E, F> cogen(genA: Gen<A>, genB: Gen<B>, genC: Gen<C>, genD: Gen<D>, genE: Gen<E>, cogenF: Cogen<F>): Cogen<(A) -> (B) -> (C) -> (D) -> (E) -> F> =
        object: Cogen<(A) -> (B) -> (C) -> (D) -> (E) -> F>() {
            override fun <X> cogen(value: (A) -> (B) -> (C) -> (D) -> (E) -> F, gen: Gen<X>): Gen<X> =
                cogen(genA, cogen(genB, cogen(genC, cogen(genD, cogen(genE, cogenF))))).cogen(value, gen)
        }   // cogen


    /**
     * Simplified replacement.
     */
    fun <A> variant(n: Long, gen: Gen<A>): Gen<A> =
        GenF.gen{size: Int -> {rand: Rand -> gen.gen(size, rand.reseed(n))}}

    /**
     * A Cogen for booleans.
     */
    val cogenBoolean: Cogen<Boolean>
        get() = object: Cogen<Boolean>() {
            override fun <X> cogen(value: Boolean, gen: Gen<X>): Gen<X> =
                variant(if(value) 0L else 1L, gen)
        }

    /**
     * A Cogen for integers.
     */
    val cogenInt: Cogen<Int>
        get() = object: Cogen<Int>() {
            override fun <X> cogen(value: Int, gen: Gen<X>): Gen<X> =
                variant(if (value >= 0) 2L * value else -2L * value + 1L, gen)
        }

    /**
     * A Cogen for longs
     */
    val cogenLong: Cogen<Long>
        get() = object: Cogen<Long>() {
            override fun <X> cogen(value: Long, gen: Gen<X>): Gen<X> =
                variant(if (value >= 0L) 2L + value else -2L * value + 1L, gen)
        }

    /**
     * A Cogen for floats.
     */
    val cogenFloat: Cogen<Float>
        get() = object: Cogen<Float>() {
            override fun <X> cogen(value: Float, gen: Gen<X>): Gen<X> =
                cogenInt.cogen(value.toInt(), gen)
        }

    /**
     * A Cogen for doubles.
     */
    val cogenDouble: Cogen<Double>
        get() = object: Cogen<Double>() {
            override fun <X> cogen(value: Double, gen: Gen<X>): Gen<X> =
                cogenLong.cogen(value.toLong(), gen)
        }

    /**
     * A Cogen for characters.
     */
    val cogenChar: Cogen<Char>
        get() = object: Cogen<Char>() {
            override fun <X> cogen(value: Char, gen: Gen<X>): Gen<X> =
                variant(value.toLong() shl 1, gen)
        }

    /**
     * A Cogen for a Pair.
     */
    fun <A, B> cogenPair(cogenA: Cogen<A>, cogenB: Cogen<B>): Cogen<Pair<A, B>> = object: Cogen<Pair<A, B>>() {
        override fun <X> cogen(value: Pair<A, B>, gen: Gen<X>): Gen<X> =
            cogenA.cogen(value.first, cogenB.cogen(value.second, gen))
    }

    /**
     * A Cogen for the Identity type.
     *
     * @param cogenA            a cogen for the type of the identity value
     * @return                  a cogen for the identity value
     */
    fun <A> cogenIdentity(cogenA: Cogen<A>): Cogen<Identity<A>> = object: Cogen<Identity<A>>() {
        override fun <X> cogen(value: Identity<A>, gen: Gen<X>): Gen<X> =
            variant(0, cogenA.cogen(value.value, gen))
    }

    /**
     * A Cogen for the Option type.
     *
     * @param cogenA            a cogen for the type of the optional value
     * @return                  a cogen for the optional value
     */
    fun <A> cogenOption(cogenA: Cogen<A>): Cogen<Option<A>> = object: Cogen<Option<A>>() {
        override fun <X> cogen(value: Option<A>, gen: Gen<X>): Gen<X> =
                if (value.isEmpty()) variant(0, gen) else variant(1, cogenA.cogen(value.get(), gen))
    }

    /**
     * A Cogen for the disjoint union.
     *
     * @param cogenA            a cogen for one side of the disjoint union
     * @param cogenB            a cogen for one side of the disjoint union
     * @return                  a cogen for the disjoint union
     */
    fun <A, B> cogenEither(cogenA: Cogen<A>, cogenB: Cogen<B>): Cogen<Either<A, B>> = object: Cogen<Either<A, B>>() {
        override fun <X> cogen(value: Either<A, B>, gen: Gen<X>): Gen<X> =
            when (value) {
                is Left -> variant(0, cogenA.cogen(value.value, gen))
                is Right -> variant(1, cogenB.cogen(value.value, gen))
            }
    }   // cogenEither

    /**
     * A Cogen for lists.
     *
     * @param cogenA            a cogen for the elements of the list
     * @return                  cogen for lists
     */
    fun <A> cogenList(cogenA: Cogen<A>): Cogen<List<A>> = object: Cogen<List<A>>() {
        override fun <X> cogen(value: List<A>, gen: Gen<X>): Gen<X> =
            when (value) {
                is Nil -> variant(0, gen)
                is Cons -> variant(1, cogenA.cogen(value.head(), cogen(value.tail(), gen)))
            }
    }   // cogenList

    /**
     * A Cogen for non empty lists.
     *
     * @param cogenA            a cogen for the elements of the non empty list
     * @return                  cogen for non empty lists
     */
    fun <A> cogenNonEmptyList(cogenA: Cogen<A>): Cogen<NonEmptyList<A>> = object: Cogen<NonEmptyList<A>>() {
        override fun <X> cogen(value: NonEmptyList<A>, gen: Gen<X>): Gen<X> =
                cogenList(cogenA).cogen(value.toList(), gen)
    }

    /**
     * A cogen for arrays.
     *
     * @param cogenA            a cogen for the elements of the array
     * @return                  a cogen for arrays
     */
    fun <A> cogenArray(cogenA: Cogen<A>): Cogen<Array<A>> = object: Cogen<Array<A>>() {
        override fun <X> cogen(value: Array<A>, gen: Gen<X>): Gen<X> =
            cogenList(cogenA).cogen(ListF.fromArray(value), gen)
    }

    /**
     * A cogen for hash maps.
     *
     * @param cogenK            a cogen for the map keys
     * @param cogenV            a cogen for the map values
     * @return                  cogen for hash maps
     */
    fun <K : Comparable<K>, V> cogenMap(cogenK: Cogen<K>, cogenV: Cogen<V>): Cogen<Map<K, V>> = object: Cogen<Map<K, V>>() {
        override fun <X> cogen(value: Map<K, V>, gen: Gen<X>): Gen<X> =
            cogenList(cogenPair(cogenK, cogenV)).cogen(MapF.toList(value), gen)
    }   // cogenMap

    /**
     * A cogen for strings.
     */
    val cogenString: Cogen<String>
        get() = object: Cogen<String>() {
            override fun <X> cogen(value: String, gen: Gen<X>): Gen<X> =
                cogenList(cogenChar).cogen(ListF.from(value), gen)
        }

}   // CogenF

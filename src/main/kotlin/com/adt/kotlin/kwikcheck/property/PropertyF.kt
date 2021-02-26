package com.adt.kotlin.kwikcheck.property

/**
 * A property represents an algebraic property about a code fragment that may be
 *   checked for its truth. For example, the commutative law states that for any
 *   two numeric values X and Y then X + Y is equivalent to Y + X. This statement
 *   is a (algebraic) property or proposition that, when checked, will at least
 *   fail to be falsified - since there does not exist a counter-example to the statement.
 *
 * A property is represented by a function from an Int (the size) to a function
 *   from a Rand (the random number generator) to a value of type Result (eg, PROVEN,
 *   FALSIFIED, etc). The function is used lazily so that, for example, member
 *   function map applies the transformation parameter to the underlying function
 *   without executing anything.
 *
 * @author	                    Ken Barclay
 * @since                       April 2020
 */

import com.adt.kotlin.kats.data.immutable.list.List
import com.adt.kotlin.kats.data.immutable.list.ListF
import com.adt.kotlin.kats.data.immutable.option.Option
import com.adt.kotlin.kats.data.immutable.option.OptionF
import com.adt.kotlin.kats.data.immutable.option.getOrElse

import com.adt.kotlin.kwikcheck.argument.ArgumentF
import com.adt.kotlin.kwikcheck.generator.Gen
import com.adt.kotlin.kwikcheck.random.Rand
import com.adt.kotlin.kwikcheck.result.FrequencyMap
import com.adt.kotlin.kwikcheck.result.FrequencyMapF
import com.adt.kotlin.kwikcheck.result.Result
import com.adt.kotlin.kwikcheck.result.ResultF
import com.adt.kotlin.kwikcheck.shrink.Shrink
import com.adt.kotlin.kwikcheck.shrink.ShrinkF



// ---------- extension functions ------------------------

infix fun Boolean.implies(prop: Property): Property =
    if (this) prop else Property{_ ->
        {_ ->
            ResultF.noResult()
        }
    }


infix fun Boolean.implies(prop: () -> Property): Property =
    if (this) prop() else Property{_ ->
        {_ ->
            ResultF.noResult()
        }
    }



object PropertyF {

    val MINSUCCESSFUL: Int = 100    // default sizes
    val MAXDISCARDED: Int = 500
    val MINSIZE: Int = 0
    val MAXSIZE: Int = 100

    /**
     * Combine properties into one, which is true if and only if all the
     *   properties are true.
     *
     * @param props             sequence of properties to combine
     * @return                  the combined property
     */
    fun all(vararg props: Property): Property =
        props.fold(proven){acc, prop -> acc.and(prop)}

    fun all(props: List<Property>): Property =
        props.foldLeft(proven){acc -> {prop -> acc.and(prop)}}

    /**
     * Universal quantifier for an explicit generator. Does not shrink failed
     *   test cases.
     *
     * @param gen               the generator to produces values from to produce the property with
     * @param f                 the function to produce properties with results
     * @return                  a property where its result is derived from universal quantification across the application of its arguments
     */
    fun <T> forAll(gen: Gen<T>, f: (T) -> Property): Property = forAll(gen, ShrinkF.empty(), f)

    /**
     * Universal quantifier for two explicit generator. Does not shrink failed
     *   test cases.
     *
     * @param gen1              the generator to produces values from to produce the property with
     * @param gen2              the generator to produces values from to produce the property with
     * @param f                 the function to produce properties with results
     * @return                  a property where its result is derived from universal quantification across the application of its arguments
     */
    fun <T1, T2> forAll(gen1: Gen<T1>, gen2: Gen<T2>, f: (T1, T2) -> Property): Property =
        forAll(gen1){t1 -> forAll(gen2){t2 -> f(t1, t2)}}

    /**
     * Universal quantifier for three explicit generator. Does not shrink failed
     *   test cases.
     *
     * @param gen1              the generator to produces values from to produce the property with
     * @param gen2              the generator to produces values from to produce the property with
     * @param gen3              the generator to produces values from to produce the property with
     * @param f                 the function to produce properties with results
     * @return                  a property where its result is derived from universal quantification across the application of its arguments
     */
    fun <T1, T2, T3> forAll(gen1: Gen<T1>, gen2: Gen<T2>, gen3: Gen<T3>, f: (T1, T2, T3) -> Property): Property =
        forAll(gen1){t1 -> forAll(gen2, gen3){t2, t3 -> f(t1, t2, t3)}}

    /**
     * Universal quantifier for four explicit generator. Does not shrink failed
     *   test cases.
     *
     * @param gen1              the generator to produces values from to produce the property with
     * @param gen2              the generator to produces values from to produce the property with
     * @param gen3              the generator to produces values from to produce the property with
     * @param gen4              the generator to produces values from to produce the property with
     * @param f                 the function to produce properties with results
     * @return                  a property where its result is derived from universal quantification across the application of its arguments
     */
    fun <T1, T2, T3, T4> forAll(gen1: Gen<T1>, gen2: Gen<T2>, gen3: Gen<T3>, gen4: Gen<T4>, f: (T1, T2, T3, T4) -> Property): Property =
        forAll(gen1){t1 -> forAll(gen2, gen3, gen4){t2, t3, t4 -> f(t1, t2, t3, t4)}}

    /**
     * Universal quantifier for five explicit generator. Does not shrink failed
     *   test cases.
     *
     * @param gen1              the generator to produces values from to produce the property with
     * @param gen2              the generator to produces values from to produce the property with
     * @param gen3              the generator to produces values from to produce the property with
     * @param gen4              the generator to produces values from to produce the property with
     * @param gen5              the generator to produces values from to produce the property with
     * @param f                 the function to produce properties with results
     * @return                  a property where its result is derived from universal quantification across the application of its arguments
     */
    fun <T1, T2, T3, T4, T5> forAll(gen1: Gen<T1>, gen2: Gen<T2>, gen3: Gen<T3>, gen4: Gen<T4>, gen5: Gen<T5>, f: (T1, T2, T3, T4, T5) -> Property): Property =
        forAll(gen1){t1 -> forAll(gen2, gen3, gen4, gen5){t2, t3, t4, t5 -> f(t1, t2, t3, t4, t5)}}



    /**
     * Returns a property where its result is derived from universal quantification across the
     *   application of its arguments.
     *
     * @param gen               the generator to produces values from to produce the property with
     * @param shrink            the shrink strategy to use upon falsification
     * @param f                 the function to produce properties with results
     * @return                  a property where its result is derived from universal quantification across the application of its arguments
     */
    fun <T> forAll(gen: Gen<T>, shrink: Shrink<T>, f: (T) -> Property): Property = forAllShrink(gen, shrink, f)

    /**
     * Returns a property where its result is derived from universal quantification across the
     *   application of its arguments.
     *
     * @param gen1              the generator to produces values from to produce the property with
     * @param gen2              the generator to produces values from to produce the property with
     * @param shrink1           the shrink strategy to use upon falsification
     * @param shrink2           the shrink strategy to use upon falsification
     * @param f                 the function to produce properties with results
     * @return                  a property where its result is derived from universal quantification across the application of its arguments
     */
    fun <T1, T2> forAll(gen1: Gen<T1>, shrink1: Shrink<T1>, gen2: Gen<T2>, shrink2: Shrink<T2>, f: (T1, T2) -> Property): Property =
        forAll(gen1, shrink1){t1 -> forAll(gen2, shrink2){t2 -> f(t1, t2)}}

    /**
     * Returns a property where its result is derived from universal quantification across the
     *   application of its arguments.
     *
     * @param gen1              the generator to produces values from to produce the property with
     * @param gen2              the generator to produces values from to produce the property with
     * @param gen3              the generator to produces values from to produce the property with
     * @param shrink1           the shrink strategy to use upon falsification
     * @param shrink2           the shrink strategy to use upon falsification
     * @param shrink3           the shrink strategy to use upon falsification
     * @param f                 the function to produce properties with results
     * @return                  a property where its result is derived from universal quantification across the application of its arguments
     */
    fun <T1, T2, T3> forAll(gen1: Gen<T1>, shrink1: Shrink<T1>, gen2: Gen<T2>, shrink2: Shrink<T2>, gen3: Gen<T3>, shrink3: Shrink<T3>, f: (T1, T2, T3) -> Property): Property =
        forAll(gen1, shrink1){t1 -> forAll(gen2, shrink2, gen3, shrink3){t2, t3 -> f(t1, t2, t3)}}

    /**
     * Returns a property where its result is derived from universal quantification across the
     *   application of its arguments.
     *
     * @param gen1              the generator to produces values from to produce the property with
     * @param gen2              the generator to produces values from to produce the property with
     * @param gen3              the generator to produces values from to produce the property with
     * @param gen4              the generator to produces values from to produce the property with
     * @param shrink1           the shrink strategy to use upon falsification
     * @param shrink2           the shrink strategy to use upon falsification
     * @param shrink3           the shrink strategy to use upon falsification
     * @param shrink4           the shrink strategy to use upon falsification
     * @param f                 the function to produce properties with results
     * @return                  a property where its result is derived from universal quantification across the application of its arguments
     */
    fun <T1, T2, T3, T4> forAll(gen1: Gen<T1>, shrink1: Shrink<T1>, gen2: Gen<T2>, shrink2: Shrink<T2>, gen3: Gen<T3>, shrink3: Shrink<T3>, gen4: Gen<T4>, shrink4: Shrink<T4>, f: (T1, T2, T3, T4) -> Property): Property =
        forAll(gen1, shrink1){t1 -> forAll(gen2, shrink2, gen3, shrink3, gen4, shrink4){t2, t3, t4 -> f(t1, t2, t3, t4)}}

    /**
     * Returns a property where its result is derived from universal quantification across the
     *   application of its arguments.
     *
     * @param gen1              the generator to produces values from to produce the property with
     * @param gen2              the generator to produces values from to produce the property with
     * @param gen3              the generator to produces values from to produce the property with
     * @param gen4              the generator to produces values from to produce the property with
     * @param gen5              the generator to produces values from to produce the property with
     * @param shrink1           the shrink strategy to use upon falsification
     * @param shrink2           the shrink strategy to use upon falsification
     * @param shrink3           the shrink strategy to use upon falsification
     * @param shrink4           the shrink strategy to use upon falsification
     * @param shrink5           the shrink strategy to use upon falsification
     * @param f                 the function to produce properties with results
     * @return                  a property where its result is derived from universal quantification across the application of its arguments
     */
    fun <T1, T2, T3, T4, T5> forAll(gen1: Gen<T1>, shrink1: Shrink<T1>, gen2: Gen<T2>, shrink2: Shrink<T2>, gen3: Gen<T3>, shrink3: Shrink<T3>, gen4: Gen<T4>, shrink4: Shrink<T4>, gen5: Gen<T5>, shrink5: Shrink<T5>, f: (T1, T2, T3, T4, T5) -> Property): Property =
        forAll(gen1, shrink1){t1 -> forAll(gen2, shrink2, gen3, shrink3, gen4, shrink4, gen5, shrink5){t2, t3, t4, t5 -> f(t1, t2, t3, t4, t5)}}



    val exception: Property = Property{_: Int -> {_: Rand -> ResultF.exception(ListF.empty(), Exception("exception property"))}}
    val falsified: Property = Property{_: Int -> {_: Rand -> ResultF.falsified(ListF.empty())}}
    val noResult: Property = Property{_: Int -> {_: Rand -> ResultF.noResult()}}
    val proven: Property = Property{_: Int -> {_: Rand -> ResultF.proven(ListF.empty())}}
    val unfalsified: Property = Property{_: Int -> {_: Rand -> ResultF.unfalsified(ListF.empty(), FrequencyMapF.empty, FrequencyMapF.empty)}}

    /**
     * Returns a property that has a result of exception, if the evaluation of the given property
     *   throws an exception; otherwise, the given property is returned.
     *
     * @param prop              a property to evaluate to check for an exception
     * @return                  a property that has a result of exception, if the evaluation of the given property throws an exception; otherwise, the given property is returned
     */
    fun exception(prop: Property): Property = prop

    /**
     * Constructs a property from a generator of results.
     *
     * @param gen               the generator of results to constructor a property with
     * @return                  a property from a generator of results
     */
    fun fromGen(gen: Gen<Result>): Property =
        Property{size -> {rand -> gen.apply(size, rand)}}

    /**
     * Returns a property that produces a result only if the given condition satisfies. The result
     *   will be taken from the given property.
     *
     * @param b                 the condition that, if satisfied, produces the given property
     * @param prop              the property to return if the condition satisfies
     * @return                  a property that produces a result only if the given condition satisfies
     */
    fun implies(b: Boolean, prop: Property): Property {
        return if(b)
            prop
        else
            Property{_ ->
                {_ ->
                    ResultF.noResult()
                }
            }
    }   // implies

    /**
     * Returns a property from the given function.
     *
     * @param func              the function to construct the returned property with
     * @return                  a property from the given function
     */
    fun prop(func: PropertyFunc): Property = Property(func)

    /**
     * Returns a property that always has the given result.
     *
     * @param result            the result of the returned property
     * @return                  a property that always has the given result
     *
    fun prop(result: Result): Property =
    Property{_ ->
    {_ ->
    result
    }
    }   // prop
     *****/

    /**
     * Returns a property that is either proven (the given condition satisfies) or falsified
     *   otherwise.
     *
     * @param b                 the condition that, if satisfied, returns a property that is proven; otherwise, the property is falsified
     * @return                  a property that is either proven (the given condition satsifies) or falsified otherwise
     */
    fun prop(b: Boolean): Property {
        fun prop(result: Result): Property =
            Property{_ ->
                {_ ->
                    result
                }
            }   // prop

        return if(b)
            prop(ResultF.proven(ListF.empty()))
        else
            prop(ResultF.falsified(ListF.empty()))
    }   // prop



// ---------- implementation ------------------------------

    /**
     * Returns a property where its result is derived from universal quantification across the
     *   application of its arguments.
     *
     * @param gen               the generator to produce the property
     * @param shrink            the shrink strategy to use on falsification
     * @param f                 the function to produce properties with results
     * @return                  a property where its result is derived from universal quantification across the application of its arguments
     */
    private fun <T> forAllShrink(gen: Gen<T>, shrink: Shrink<T>, f: (T) -> Property): Property {

        var labelsFM: FrequencyMap = FrequencyMapF.empty
        var collectedFM: FrequencyMap = FrequencyMapF.empty

        /**
         * Return false if the opt parameter is None otherwise return true if the Result
         *   part of Some is failed (FALSIFIED or EXCEPTION).
         */
        fun isFailed(opt: Option<Pair<T, Result>>): Boolean =
            when (opt) {
                is Option.None -> false
                is Option.Some -> opt.value.second.failed()
            }   // failed

        /**
         * Find the optional pair of T and Result derived from applying function f
         *   to the content of the List ts. If the resulting List is empty then
         *   return None else wrap the first of the pair that reports isFailed or
         *   the first of the Lis if none in a Some.
         */
        fun findFirst(ts: List<T>, shrinks: Int, size: Int, rand: Rand): Option<Pair<T, Result>> {
            val results: List<Option<Pair<T, Result>>> = ts.map{t: T ->
                val result: Result = PropertyF.exception(f(t)).apply(size, rand)
                labelsFM = if (result.labels.isEmpty()) labelsFM else labelsFM.add(result.labels)
                collectedFM = if (result.collected.isEmpty()) collectedFM else collectedFM.add(result.collected)

                val res: Result = result.copy(labels = labelsFM, collected = collectedFM)
                val opt: Option<Result> = res.toOption()
                when (opt) {
                    is Option.None -> OptionF.none()
                    is Option.Some -> OptionF.some(
                        Pair(t, opt.value.provenAsUnfalsified().addArgument(ArgumentF.arg(t as Any, shrinks)))
                    )
                }
            }

            return if (results.isEmpty())
                OptionF.none()
            else
                results.find{opt -> isFailed(opt)}.getOrElse(results.head())
        }   // first

        return prop{size: Int ->
            {rand: Rand ->
                var opt: Option<Pair<T, Result>> = findFirst(ListF.singleton(gen.apply(size, rand)), 0, size, rand)
                if (isFailed(opt)) {
                    var shrinks: Int = 0
                    var or: Option<Result>
                    do {
                        shrinks++
                        or = opt.map{pr -> pr.second}
                        opt = findFirst(shrink.shrink(opt.get().first), shrinks, size, rand)
                    } while (isFailed(opt))
                    ResultF.noResult(or)
                } else {
                    val res: Result = ResultF.noResult(opt.map{pr -> pr.second})
                    res
                }
            }
        }
    }   // forAllShrink

}   // PropertyF

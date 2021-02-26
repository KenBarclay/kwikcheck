package com.adt.kotlin.kwikcheck.property

/**
 * A property represents an algebraic property about a code fragment that may be
 *   checked for its truth. For example, the commutative law states that for any
 *   two numeric values X and Y then X + Y is equivalent to Y + X. This statement
 *   is a (algebraic) property or proposition that, when checked, will at least
 *   fail to be falsified - since there does not exist a counter-example to the
 *   statement.
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

import com.adt.kotlin.kwikcheck.checkresult.CheckResult
import com.adt.kotlin.kwikcheck.checkresult.CheckResultF.exhausted
import com.adt.kotlin.kwikcheck.checkresult.CheckResultF.falsified
import com.adt.kotlin.kwikcheck.checkresult.CheckResultF.genException
import com.adt.kotlin.kwikcheck.checkresult.CheckResultF.passed
import com.adt.kotlin.kwikcheck.checkresult.CheckResultF.propException
import com.adt.kotlin.kwikcheck.checkresult.CheckResultF.proven
import com.adt.kotlin.kwikcheck.generator.Gen
import com.adt.kotlin.kwikcheck.property.PropertyF.MAXDISCARDED
import com.adt.kotlin.kwikcheck.property.PropertyF.MAXSIZE
import com.adt.kotlin.kwikcheck.property.PropertyF.MINSIZE
import com.adt.kotlin.kwikcheck.property.PropertyF.MINSUCCESSFUL
import com.adt.kotlin.kwikcheck.random.Rand
import com.adt.kotlin.kwikcheck.random.RandF
import com.adt.kotlin.kwikcheck.result.FrequencyMap
import com.adt.kotlin.kwikcheck.result.FrequencyMapF
import com.adt.kotlin.kwikcheck.result.Result
import com.adt.kotlin.kwikcheck.result.ResultF



typealias PropertyFunc = (Int) -> (Rand) -> Result

class Property(val func: PropertyFunc) {

    /**
     * Perform a conjunction of this property with the given property.
     *
     * @param prop              the property to perform the conjunction with
     * @return                  a conjunction of this property with the given property
     */
    infix fun and(prop: Property): Property = PropertyF.fromGen(this.gen().bind{res1 ->
        prop.gen().bind{res2 ->
            Gen{_ ->
                {_: Rand ->
                    if(res1.isException() || res1.isFalsified())        // failed
                        res1
                    else if (res2.isException() || res2.isFalsified())  // failed
                        res2
                    else if(res1.isProven() || res1.isUnfalsified())    // passed
                        res2
                    else if(res2.isProven() || res2.isUnfalsified())    // passed
                        res1
                    else
                        ResultF.noResult()
                }
            }
        }
    })  // and

    /**
     * Perform a disjunction of this property with the given property.
     *
     * @param prop              the property to perform the disjunction with
     * @return                  a disjunction of this property with the given property
     */
    infix fun or(prop: Property): Property = PropertyF.fromGen(this.gen().bind{res1 ->
        prop.gen().bind{res2 ->
            Gen{_ ->
                {_: Rand ->
                    if(res1.isProven() || res1.isUnfalsified())         // passed
                        res1
                    else if(res2.isProven() || res2.isUnfalsified())    // passed
                        res2
                    else if(res1.isException() || res1.isFalsified())   // failed
                        res1
                    else if(res2.isException() || res2.isFalsified())   // failed
                        res2
                    else
                        ResultF.noResult()
                }
            }
        }
    })  // or


    /**
     * Perform a sequence of this property with the given property. The returned
     *   property holds if and only if this property and the given property also hold.
     *   If one property does not hold, but the other does, then the returned property
     *   will produce the same result as the property that holds.
     *
     * @param p                 the property to sequence this property with
     * @return                  a sequence of this property with the given property
     */
    fun sequence(prop: Property): Property = PropertyF.fromGen(this.gen().bind{res1 ->
        prop.gen().bind{res2 ->
            Gen{_ ->
                {_: Rand ->
                    if(res1.isException() || res1.isProven() || res1.isUnfalsified())
                        res1
                    else if(res2.isException() || res2.isProven() || res2.isUnfalsified())
                        res2
                    else if(res1.isFalsified())
                        res2
                    else if(res2.isFalsified())
                        res1
                    else
                        ResultF.noResult()
                }
            }
        }
    })  // sequence



    /**
     * Return the result of applying the given size and random generator.
     *
     * @param size              the size to use to obtain a result
     * @param rand              the random generator to use to obtain a result
     * @return                  the result of applying the given size and random generator
     */
    fun apply(size: Int, rand: Rand): Result = func(size)(rand)

    /**
     * Return a generator of the result from this property.
     *
     * @return                  a generator of the result from this property
     */
    fun gen() : Gen<Result> =
        Gen{size: Int -> {rand: Rand -> this.apply(size, rand)}}

    /**
     * Add an additional label to the result of this property.
     *
     * @param text              the label to add
     * @return                  an updated Property
     */
    fun label(text: String): Property {
        return Property{size: Int ->
            {rand: Rand ->
                val res: Result = this.apply(size, rand)
                res.label(text)
            }
        }
    }   // label

    /**
     * Gather all the values passed through this function to produce a frequency of each value.
     *
     * @param any               the collected value
     * @return                  an updated property
     */
    fun collect(any: Any): Property =
        Property{size: Int ->
            {rand: Rand ->
                val res: Result = this.apply(size, rand)
                res.collect(any.toString())
            }
        }   // collect

    /**
     * Gather all the values passed through this function to produce a frequency of each value.
     *
     * @param b                 determine which value to collect
     * @param ifTrue            the value to collect if b is true
     * @param ifFalse           the value to collect if b is false
     * @return                  an updated property
     */
    fun classify(b: Boolean, ifTrue: Any, ifFalse: Any): Property =
        if (b) collect(ifTrue) else collect(ifFalse)

    /**
     * Gather all the values passed through this function to produce a frequency of each value.
     *   If the boolean b is false then the collected value is Unit.
     *
     * @param b                 determine which value to collect
     * @param ifTrue            the value to collect if b is true
     * @return                  an updated property
     */
    fun classify(b: Boolean, ifTrue: Any): Property =
        if (b) collect(ifTrue) else collect(Unit)

    /**
     * XXX
     */
    fun map(f: (Result) -> Result): Property =
        Property{size: Int ->
            {rand: Rand ->
                f(this.apply(size, rand))
            }
        }

    /**
     * XXX
     */
    fun bind(f: (Result) -> Property): Property =
        Property{size: Int ->
            {rand: Rand ->
                val result: Result = this.apply(size, rand)
                val rnd: Rand = rand.reseed(size.toLong())
                f(result).apply(size, rnd)
            }
        }


    /**
     * Checks this property using the given arguments and produces a result.
     *
     * @param rand              the random generator to use for checking
     * @param minSuccessful     the minimum number of successful tests before a result is reached
     * @param maxDiscarded      the maximum number of tests discarded because they did not satisfy pre-conditions
     * @param minSize           the minimum size to use for checking
     * @param maxSize           the maximum size to use for checking
     * @param trace             trace the execution
     * @return                  a result after checking this property
     */
    fun check(rand: Rand = RandF.standard, minSuccessful: Int = MINSUCCESSFUL, maxDiscarded: Int = MAXDISCARDED, minSize: Int = MINSIZE, maxSize: Int = MAXSIZE, trace: Boolean = false): CheckResult {
        var succeeded: Int = 0      // succeeded
        var discarded: Int = 0      // discarded
        var sz: Float = minSize.toFloat()

        var labelsFM: FrequencyMap = FrequencyMapF.empty
        var collectedFM: FrequencyMap = FrequencyMapF.empty

        while (true) {
            val size: Float = if(succeeded == 0 && discarded == 0) minSize.toFloat() else sz + (maxSize - sz)/(minSuccessful - succeeded)

            try {
                val res: Result = this.apply(size.toInt(), rand)
                labelsFM = if (res.labels.isEmpty()) labelsFM else labelsFM.add(res.labels)
                collectedFM = if (res.collected.isEmpty()) collectedFM else collectedFM.add(res.collected)

                if (trace) {
                    println("check: trace - succeeded(s): ${succeeded} discarded(d): ${discarded} sz: ${sz} size: ${size}; res: ${res}")
                }

                if (res.isNoResult()) {
                    if (discarded + 1 >= maxDiscarded) {
                        return exhausted(succeeded, discarded + 1, labelsFM)
                    } else {
                        sz = size
                        discarded++
                    }
                } else if (res.isProven()) {
                    return proven(res.arguments.get(), succeeded + 1, discarded)
                } else if (res.isUnfalsified()) {
                    if (succeeded + 1 >= minSuccessful) {
                        return passed(succeeded + 1, discarded, res.labels, res.collected)
                    } else {
                        sz = size
                        succeeded++
                    }
                } else if (res.isFalsified()) {
                    return falsified(res.arguments.get(), succeeded, discarded, res.labels, res.collected)
                } else {
                    return propException(res.arguments.get(), res.th.get(), succeeded, discarded)
                }
            } catch(t: Throwable) {
                return genException(t, succeeded, discarded)
            }
        }
    }   // check

    /**
     * Check this property using a standard random generator, the given minimum
     *   successful checks, 500 maximum discarded tests, minimum size of 0, maximum size of 100.
     *
     * @param minSuccessful     the minimum number of successful tests before a result is reached
     * @return                  a result after checking this property
     */
    fun minSuccessful(minSuccessful: Int): CheckResult =
        check(minSuccessful = minSuccessful)

    /**
     * Check this property using the given random generator, the given minimum
     *   successful checks, 500 maximum discarded tests, minimum size of 0, maximum size of 100.
     *
     * @param rand              the random generator
     * @param minSuccessful     the minimum number of successful tests before a result is reached
     * @return                  a result after checking this property
     */
    fun minSuccessful(rand: Rand, minSuccessful: Int): CheckResult =
        check(rand = rand, minSuccessful = minSuccessful)

    /**
     * Check this property using a standard random generator, 100 minimum
     *   successful checks, the given maximum discarded tests, minimum size of 0, maximum size of 100.
     *
     * @param maxDiscarded      the maximum number of tests discarded because they did not satisfy pre-conditions
     * @return                  a result after checking this property
     */
    fun maxDiscarded(maxDiscarded: Int): CheckResult =
        check(maxDiscarded = maxDiscarded)

    /**
     * Check this property using the given random generator}, 100 minimum
     *   successful checks, the given maximum discarded tests, minimum size of 0, maximum size of 100.
     *
     * @param rand              the random generator
     * @param maxDiscarded      the maximum number of tests discarded because they did not satisfy pre-conditions
     * @return                  a result after checking this property
     */
    fun maxDiscarded(rand: Rand, maxDiscarded: Int): CheckResult =
        check(rand = rand, maxDiscarded = maxDiscarded)

    /**
     * Check this property using a standard random generator, 100 minimum
     *   successful checks, 500 maximum discarded tests, the given minimum size, maximum size of 100.
     *
     * @param minSize           the minimum size to use for checking
     * @return                  a result after checking this property
     */
    fun minSize(minSize: Int): CheckResult =
        check(minSize = minSize)

    /**
     * Check this property using the given random generator, 100 minimum
     *   successful checks, 500 maximum discarded tests, the given minimum size, maximum size of 100.
     *
     * @param rand              the random generator
     * @param minSize           the minimum size to use for checking
     * @return                  a result after checking this property
     */
    fun minSize(rand: Rand, minSize: Int): CheckResult =
        check(rand = rand, minSize = minSize)


}   // Property

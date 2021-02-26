package com.adt.kotlin.kwikcheck.result

/**
 * The result of evaluating a property.
 *
 * @author	                    Ken Barclay
 * @since                       April 2020
 */

import com.adt.kotlin.kats.data.immutable.list.List
import com.adt.kotlin.kats.data.immutable.list.append

import com.adt.kotlin.kats.data.immutable.option.Option
import com.adt.kotlin.kats.data.immutable.option.OptionF.none
import com.adt.kotlin.kats.data.immutable.option.OptionF.some

import com.adt.kotlin.kwikcheck.argument.Argument



object ResultF {

    /**
     * Returns an EXCEPTION result.
     *
     * @param arguments         the arguments used when the exception occurred
     * @param th                the exception that occurred
     * @return                  an EXCEPTION result
     */
    fun exception(arguments: List<Argument<Any>>, th: Throwable): Result =
        Result(Result.Status.EXCEPTION, some(arguments), some(th), FrequencyMapF.empty)

    /**
     * Returns a FALSIFIED result.
     *
     * @param arguments         the arguments used during falsification
     * @return                  a FALSIFIED result
     */
    fun falsified(arguments: List<Argument<Any>>): Result =
        Result(Result.Status.FALSIFIED, some(arguments), none(), FrequencyMapF.empty)

    /**
     * Returns a result representing NORESULT.
     *
     * @return                  a result representing NORESULT
     */
    fun noResult(): Result =
        Result(Result.Status.NORESULT, none(), none(), FrequencyMapF.empty)

    /**
     * Returns a result from the given potential result.
     *
     * @param result            the potential result
     * @return                  the result that may be a NORESULT
     */
    fun noResult(result: Option<Result>): Result {
        when(result) {
            is Option.None -> return noResult()
            is Option.Some -> return result.get()
        }
    }   // noResult

    /**
     * Returns a PROVEN result.
     *
     * @param arguments         the arguments used during proof
     * @return                  a PROVEN result
     */
    fun proven(arguments: List<Argument<Any>>): Result =
        Result(Result.Status.PROVEN, some(arguments), none(), FrequencyMapF.empty)

    /**
     * Returns an UNFALSIFIED result.
     *
     * @param arguments         the arguments used during the failure of falsification
     * @return                  an UNFALSIFIED result
     */
    fun unfalsified(arguments: List<Argument<Any>>, labels: FrequencyMap, collected: FrequencyMap): Result =
        Result(Result.Status.UNFALSIFIED, some(arguments), none(), labels, collected)

    fun merge(result1: Result, result2: Result, status: Result.Status): Result {
        val args: Option<List<Argument<Any>>> = result1.arguments.bind{list1 ->
            result2.arguments.bind{list2 ->
                some(list1.append(list2))
            }
        }
        val th: Option<Throwable> = result1.th.bind{th1 ->
            result2.th.bind{th2 ->
                some(Exception("${th1.message}; ${th2.message}"))
            }
        }
        return Result(status, args, th, result1.labels.add(result2.labels), result1.collected.add(result2.collected))
    }

}   // ResultF

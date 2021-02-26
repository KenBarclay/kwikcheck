package com.adt.kotlin.kwikcheck.result

/**
 * The result of evaluating a property.
 *
 * @author	                    Ken Barclay
 * @since                       April 2020
 */

import com.adt.kotlin.kats.data.immutable.list.List
import com.adt.kotlin.kats.data.immutable.list.ListF
import com.adt.kotlin.kats.data.immutable.option.Option
import com.adt.kotlin.kats.data.immutable.option.OptionF

import com.adt.kotlin.kwikcheck.argument.Argument



/**
 * Result:
 *
 * @param status                the type of result, eg FALSIFIED, PROVEN, ...
 * @param arguments             the property arguments
 * @param th                    a possible throwable
 * @param labels                the labels collected by the Property label action (possibly empty)
 * @param collected             the monitoring values collected by the Property collect action (possibly empty)
 */
data class Result(
    val status: Status,
    val arguments: Option<List<Argument<Any>>>,
    val th: Option<Throwable>,
    val labels: FrequencyMap = FrequencyMapF.empty,
    val collected: FrequencyMap = FrequencyMapF.empty
) {

    enum class Status {
        EXCEPTION,
        FALSIFIED,
        NORESULT,
        PROVEN,
        UNFALSIFIED
    }   // Status

    /**
     * Return true if this result is an EXCEPTION; otherwise, false.
     *
     * @return                  true if this result is an EXCEPTION; otherwise, false
     */
    fun isException(): Boolean = (status == Status.EXCEPTION)

    /**
     * Return true if this result is FALSIFIED; otherwise, false.
     *
     * @return                  true if this result is FALSIFIED; otherwise, false
     */
    fun isFalsified(): Boolean = (status == Status.FALSIFIED)

    /**
     * Return true if this result is NORESULT; otherwise, false.
     *
     * @return                  true if this result is NORESULT; otherwise, false
     */
    fun isNoResult(): Boolean = (status == Status.NORESULT)

    /**
     * Return true if this result is PROVEN; otherwise, false.
     *
     * @return                  true if this result is PROVEN; otherwise, false
     */
    fun isProven(): Boolean = (status == Status.PROVEN)

    /**
     * Return true if this result is UNFALSIFIED; otherwise false.
     *
     * @return                  true if this result is UNFALSIFIED; otherwise, false
     */
    fun isUnfalsified(): Boolean = (status == Status.UNFALSIFIED)

    /**
     * Return true if this result is falsified or an exception; otherwise false.
     *
     * @return                  true if this result is falsified or an exception; otherwise false
     */
    fun failed(): Boolean = isFalsified() || isException()

    /**
     * Return true if this result is unfalsified or proven; otherwise false.
     *
     * @return                  true if this result is unfalsified or proven; otherwise false
     */
    fun passed(): Boolean = isUnfalsified() || isProven()

    /**
     * If this result is proven, alter it to be unfalsified with the same parameters;
     *   otherwise, return this.
     *
     * @return                  if this result is proven, alter it to be unfalsified with the same arguments otherwise, return this
     */
    fun provenAsUnfalsified(): Result = if(isProven()) ResultF.unfalsified(arguments.get(), labels, collected) else this

    /**
     * Add an argument to this result.
     *
     * @param argument          the argument to add
     * @return                  a result with the new argument
     */
    fun addArgument(argument: Argument<Any>): Result =
        this.copy(arguments = arguments.map{list -> ListF.cons(argument, list)})

    /**
     * Add an additional label to this result.
     *
     * @param text              the label to add
     * @return                  a Result with an additional label
     */
    fun label(text: String): Result =
        this.copy(labels = labels.add(text))

    /**
     * Add an additional collected item to this result.
     *
     * @param text              the collected item to add
     * @return                  a Result with an additional collected item
     */
    fun collect(text: String): Result =
        this.copy(collected = collected.add(text))

    /**
     * Returns a potential result for this result. This will have a value if this result is
     *   not a NORESULT.
     *
     * @return                  a potential result for this result
     */
    fun toOption(): Option<Result> = if(this.isNoResult()) OptionF.none() else OptionF.some(this)

}   // Result

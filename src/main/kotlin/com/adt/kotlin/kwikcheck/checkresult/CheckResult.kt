package com.adt.kotlin.kwikcheck.checkresult

/**
 * An enumeration of the possible results after checking a property.
 *   A result may be in one of six states: EXHAUSTED, FALSIFIED,
 *   GENERALEXCEPTION, PASSED, PROPERTYEXCEPTION or PROVEN.
 *
 * @author	                    Ken Barclay
 * @since                       April 2020
 */

import com.adt.kotlin.kats.data.immutable.list.List
import com.adt.kotlin.kats.data.immutable.option.Option

import com.adt.kotlin.kwikcheck.argument.Argument
import com.adt.kotlin.kwikcheck.result.FrequencyMap
import com.adt.kotlin.kwikcheck.result.FrequencyMapF



/**
 * CheckResult:
 *
 * @param status                the type of CheckResult, eg FALSIFIED, PASSED, ...
 * @param args                  the property arguments
 * @param th                    a possible throwable
 * @param succeeded             the number of successful tests
 * @param discarded             the number of discarded tests
 * @param labels                the labels collected by the Property label action (possibly empty)
 * @param collected             the monitoring values collected by the Property collect action (possibly empty)
 */
class CheckResult(
    val status: Status,
    val args: Option<List<Argument<Any>>>,
    val th: Option<Throwable>,
    val succeeded: Int,
    val discarded: Int,
    val labels: FrequencyMap = FrequencyMapF.empty,
    val collected: FrequencyMap = FrequencyMapF.empty
) {

    enum class Status {
        EXHAUSTED,
        FALSIFIED,
        GENERALEXCEPTION,
        PASSED,
        PROPERTYEXCEPTION,
        PROVEN
    }   // Status

    /**
     * Return true if this result is an EXHAUSTED; otherwise, false.
     *
     * @return                  true if this result is an EXHAUSTED; otherwise, false
     */
    fun isExhausted(): Boolean = (status == Status.EXHAUSTED)

    /**
     * Return true if this result is an FALSIFIED; otherwise, false.
     *
     * @return                  true if this result is an FALSIFIED; otherwise, false
     */
    fun isFalsified(): Boolean = (status == Status.FALSIFIED)

    /**
     * Return true if this result is an GENERALEXCEPTION; otherwise, false.
     *
     * @return                  true if this result is an GENERALEXCEPTION; otherwise, false
     */
    fun isGeneralException(): Boolean = (status == Status.GENERALEXCEPTION)

    /**
     * Return true if this result is an PASSED; otherwise, false.
     *
     * @return                  true if this result is an PASSED; otherwise, false
     */
    fun isPassed(): Boolean = (status == Status.PASSED)

    /**
     * Return true if this result is an PROPERTYEXCEPTION; otherwise, false.
     *
     * @return                  true if this result is an PROPERTYEXCEPTION; otherwise, false
     */
    fun isPropertyException(): Boolean = (status == Status.PROPERTYEXCEPTION)

    /**
     * Return true if this result is an PROVEN; otherwise, false.
     *
     * @return                  true if this result is an PROVEN; otherwise, false
     */
    fun isProven(): Boolean = (status == Status.PROVEN)

    /**
     * Summarise the result.
     */
    fun summary(): String =
        when (status) {
            Status.EXHAUSTED -> "Gave up after $succeeded passed ${showTestOrTests(succeeded)} and $discarded discarded tests"

            Status.FALSIFIED -> {
                val falsifiedAfter: String = "Falsified after $succeeded passed ${showTestOrTests(succeeded)} with ${showArguments(args)}"
                val labelsFalsifiedAfter: String = if (labels.isEmpty()) "" else labels.map.keyList().makeString()
                if (labels.isEmpty()) falsifiedAfter else "$falsifiedAfter\nLabels of failing property: $labelsFalsifiedAfter"
            }

            Status.GENERALEXCEPTION -> {
                val thr: Throwable = th.fold({-> Throwable("Unknown")}, {th -> th})
                "Exception on argument generation: ${thr.message}"
            }

            Status.PASSED -> {
                val okPassed: String = if (discarded > 0)
                    "OK, passed $succeeded ${showTestOrTests(succeeded)} ${showDiscarded(discarded)}"
                else
                    "OK, passed $succeeded ${showTestOrTests(succeeded)}"

                if (!labels.isEmpty()) {
                    val ratios: List<Pair<String, Double>> = labels.getRatios()
                    val ratiosStr: String = ratios.foldLeft(""){str -> {pr -> "$str\n${(pr.second * 100).toInt()}% ${pr.first}"}}
                    "$okPassed\nLabels:$ratiosStr"
                } else if (!collected.isEmpty()) {
                    val ratios: List<Pair<String, Double>> = collected.getRatios()
                    val ratiosStr: String = ratios.foldLeft(""){str -> {pr -> "$str\n${(pr.second * 100).toInt()}% ${pr.first}"}}
                    "$okPassed\nCollected test data:$ratiosStr"
                } else
                    okPassed
            }

            Status.PROPERTYEXCEPTION -> "Exception on property evaluation with ${showArguments(args)}"

            Status.PROVEN -> "OK, property proven with: ${showArguments(args)}"
        }



    companion object {

        fun showDiscarded(discarded: Int): String =
            if (discarded > 0) "($discarded discarded)" else ""

        fun showTestOrTests(succeeded: Int): String =
            if (succeeded == 1) "test" else "tests"

        fun showArguments(arguments: Option<List<Argument<Any>>>): String =
            when (arguments) {
                is Option.None -> ""
                is Option.Some -> {
                    val argList: List<Argument<Any>> = arguments.value
                    if (argList.size() == 1)
                        "${argList.head()}"
                    else
                        "(${argList.drop(1).foldLeft("${argList.head()}"){str -> {arg -> "$str, $arg"}}})"
                }
            }   // showArguments

    }

}   // CheckResult

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
import com.adt.kotlin.kats.data.immutable.option.OptionF.none
import com.adt.kotlin.kats.data.immutable.option.OptionF.some

import com.adt.kotlin.kwikcheck.argument.Argument
import com.adt.kotlin.kwikcheck.result.FrequencyMap



object CheckResultF {

    /**
     * Return a result that the property been exhausted in checking.
     *
     * @param succeeded          the number of checks that succeeded
     * @param discarded          the number of checks that were discarded
     * @return                   a result that the property has been exhausted in checking
     */
    fun exhausted(succeeded: Int, discarded: Int, labels: FrequencyMap): CheckResult =
        CheckResult(CheckResult.Status.EXHAUSTED, none(), none(), succeeded, discarded, labels)

    /**
     * Return a result that the property has been falsified.
     *
     * @param args               the arguments used to falsify the property
     * @param succeeded          the number of checks that succeeded
     * @param discarded          the number of checks that were discarded
     * @return                   a result that the property has been falsified
     */
    fun falsified(args: List<Argument<Any>>, succeeded: Int, discarded: Int, labels: FrequencyMap, collected: FrequencyMap): CheckResult =
        CheckResult(CheckResult.Status.FALSIFIED, some(args), none(), succeeded, discarded, labels, collected)

    /**
     * Return a result that generating values to check the property threw an exception.
     *
     * @param th                 the exception that was thrown
     * @param succeeded          the number of checks that succeeded
     * @param discarded          the number of checks that were discarded
     * @return                   a result that generating values to check the property threw an exception
     */
    fun genException(th: Throwable, succeeded: Int, discarded: Int): CheckResult =
        CheckResult(CheckResult.Status.GENERALEXCEPTION, none(), some(th), succeeded, discarded)

    /**
     * Return a result that the property has passed.
     *
     * @param succeeded          the number of checks that succeeded
     * @param discarded          the number of checks that were discarded
     * @return                   a result that the property has passed
     */
    fun passed(succeeded: Int, discarded: Int, labels: FrequencyMap, collected: FrequencyMap): CheckResult =
        CheckResult(CheckResult.Status.PASSED, none(), none(), succeeded, discarded, labels, collected)

    /**
     * Return a result that checking the property threw an exception.
     *
     * @param args               the arguments used when the exception was thrown
     * @param th                 the exception that was thrown
     * @param succeeded          the number of checks that succeeded
     * @param discarded          the number of checks that were discarded
     * @return                   a result that checking the property threw an exception
     */
    fun propException(args: List<Argument<Any>>, th: Throwable, succeeded: Int, discarded: Int): CheckResult =
        CheckResult(CheckResult.Status.PROPERTYEXCEPTION, some(args), some(th), succeeded, discarded)

    /**
     * Return a result that the property has been proven.
     *
     * @param args               the arguments used to prove the property
     * @param succeeded          the number of checks that succeeded
     * @param discarded          the number of checks that were discarded
     * @return                   a result that the property has been proven
     */
    fun proven(args: List<Argument<Any>>, succeeded: Int, discarded: Int): CheckResult =
        CheckResult(CheckResult.Status.PROVEN, some(args), none(), succeeded, discarded)

}   // CheckResultF

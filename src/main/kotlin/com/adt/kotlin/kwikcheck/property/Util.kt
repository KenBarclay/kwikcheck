package com.adt.kotlin.kwikcheck.property

import com.adt.kotlin.kats.data.immutable.option.Option
import com.adt.kotlin.kats.data.immutable.option.Option.None
import com.adt.kotlin.kats.data.immutable.option.Option.Some
import com.adt.kotlin.kats.data.immutable.option.OptionF.none
import com.adt.kotlin.kats.data.immutable.option.OptionF.some
import com.adt.kotlin.kats.data.immutable.option.getOrElse

import com.adt.kotlin.kats.data.immutable.list.List

import com.adt.kotlin.kwikcheck.argument.ArgumentF

import com.adt.kotlin.kwikcheck.random.Rand
import com.adt.kotlin.kwikcheck.result.FrequencyMap
import com.adt.kotlin.kwikcheck.result.FrequencyMapF
import com.adt.kotlin.kwikcheck.result.Result


class Util<T>(val f: (T) -> Property, val size: Int, val rand: Rand) {

    fun first(ts: List<T>, shrinks: Int): Option<Pair<T, Result>> {
        val results: List<Option<Pair<T, Result>>> = ts.map{t: T ->
            val result: Result = PropertyF.exception(f(t)).apply(size, rand)
            fm = if (result.labels.isEmpty()) fm else fm.add(result.labels)
            val res: Result = result.copy(labels = fm)
            println("    Util.first: res: $res")
            println("    Util.first: fm: $fm")
            val opt: Option<Result> = res.toOption()
            when (opt) {
                is None -> none()
                is Some -> some(Pair(t, opt.value.provenAsUnfalsified().addArgument(ArgumentF.arg(t as Any, shrinks))))
            }
        }
        ////println("Util.first: results: $results")

        return if (results.isEmpty()) {
            none()
        } else
            results.find{opt -> failed(opt)}.getOrElse(results.head())

    }   // first

    fun failed(opt: Option<Pair<T, Result>>): Boolean =
        when (opt) {
            is None -> false
            is Some -> opt.value.second.failed()
        }   // failed

// ---------- properties ----------------------------------

    var fm: FrequencyMap = FrequencyMapF.empty

}   // Util

package com.adt.kotlin.kwikcheck.result

/**
 * A frequency map records result monitoring and their frequency of occurrence.
 *
 * @author	                    Ken Barclay
 * @since                       April 2020
 */

import com.adt.kotlin.kats.data.immutable.list.List
import com.adt.kotlin.kats.data.immutable.list.append
import com.adt.kotlin.kats.data.immutable.map.Map
import com.adt.kotlin.kats.data.immutable.map.MapF
import com.adt.kotlin.kats.data.immutable.map.insert
import com.adt.kotlin.kats.data.immutable.option.Option
import com.adt.kotlin.kats.data.immutable.option.getOrElse



data class FrequencyMap(val map: Map<String, Int> = MapF.empty(), val total: Int = 0) {

    fun isEmpty(): Boolean = map.isEmpty()

    fun add(text: String): FrequencyMap {
        val n: Int = when (val op = map.lookUpKey(text)) {
            is Option.None -> 1
            is Option.Some -> 1 + op.value
        }

        return FrequencyMap(map.insert(text, n), 1 + total)
    }   // add

    fun add(fm: FrequencyMap): FrequencyMap {
        val keys: List<String> = map.keyList().append(fm.map.keyList())
        val mappings: List<Pair<String, Int>> = keys.map{key ->
            Pair(key, getCount(key).getOrElse(0) + fm.getCount(key).getOrElse(0))
        }
        val mp: Map<String, Int> = mappings.foldLeft(MapF.empty()){m: Map<String, Int> ->
            {pr: Pair<String, Int> -> m.insert(pr)}
        }
        val tot: Int = total + fm.total
        return FrequencyMap(mp, tot)
    }   // add

    fun getCount(text: String): Option<Int> = map.lookUpKey(text)

    fun getCounts(): List<Pair<String, Int>> = map.toAscendingList()

    fun getRatio(text: String): Option<Double> = getCount(text).map{n: Int -> n.toDouble() / total.toDouble()}

    fun getRatios(): List<Pair<String, Double>> = getCounts().map{pr: Pair<String, Int> ->
        Pair(pr.first, pr.second.toDouble() / total.toDouble())
    }   // getRatios

}   // FrequencyMap

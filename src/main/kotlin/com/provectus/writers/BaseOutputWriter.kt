package com.provectus.writers

import com.provectus.model.Query
import com.provectus.model.Result
import com.provectus.model.results.BooleanAsNumberValueTransformer
import com.provectus.model.results.IdentityValueTransformer
import com.provectus.model.results.ValueTransformer
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

abstract class BaseOutputWriter(
    val typeNames:List<String>,
    booleanAsNumber:Boolean = false,
    val debug:Boolean = false,
    val settings:Config = ConfigFactory.empty()
) : OutputWriter {

    private val valueTransformer: ValueTransformer =
        if (booleanAsNumber) { BooleanAsNumberValueTransformer(1,0) }
        else { IdentityValueTransformer() }

    override fun write(query: Query, results:Iterable<Result>) {
        internalWrite(query, results.map{ it.copy(value = valueTransformer.apply(it.value))}.toList())
    }

    fun filterTypeNames(typeNamesMap: Map<String,String>):Map<String,String> {
        return typeNamesMap.filter { this.typeNames.isEmpty() || this.typeNames.contains(it.key) }
    }

    protected abstract fun internalWrite(query: Query, results: List<Result>)



}
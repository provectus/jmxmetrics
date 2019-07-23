package com.provectus.writers

import com.provectus.model.Query
import com.provectus.model.Result
import com.provectus.model.output.ResultSerializer
import com.provectus.model.output.ToStringResultSerializer
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

class StdOutWriter(
    typeNames:List<String>,
    booleanAsNumber:Boolean = false,
    debug:Boolean = false,
    settings:Config = ConfigFactory.empty(),
    private val resultSerializer:ResultSerializer<String> = ToStringResultSerializer.DEFAULT
)  : BaseOutputWriter(typeNames, booleanAsNumber, debug, settings) {

    companion object : OutputWriterFactory {
        override fun create(
            typeNames:List<String>,
            booleanAsNumber:Boolean,
            debug:Boolean,
            settings:Config
        ): OutputWriter {
            return StdOutWriter(typeNames, booleanAsNumber, debug, settings)
        }

    }

    override fun internalWrite(query: Query, results: List<Result>) {
        for (r in results) {
            val s = resultSerializer.serialize(query, r)
            println(s)
        }
    }

    override val name: String
        get() = StdOutWriter::class.java.name

    override fun close() {

    }


}
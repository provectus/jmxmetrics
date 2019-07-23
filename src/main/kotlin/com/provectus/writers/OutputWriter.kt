package com.provectus.writers

import com.provectus.model.Query
import com.provectus.model.Result
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.Closeable

interface OutputWriter : Closeable {
    val name:String
    fun write(query: Query, results:Iterable<Result>)
}

interface OutputWriterFactory {
    fun create(
        typeNames:List<String>,
        booleanAsNumber:Boolean = false,
        debug:Boolean = false,
        settings: Config = ConfigFactory.empty()
    ):OutputWriter
}
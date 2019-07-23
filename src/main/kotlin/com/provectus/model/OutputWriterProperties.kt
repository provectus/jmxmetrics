package com.provectus.model

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.Serializable

data class OutputWriterProperties(
    val className:String,
    val typeNames:List<String> = emptyList(),
    val debug:Boolean = false,
    val settings:Config = ConfigFactory.empty()
) : Serializable
package com.provectus.model

import java.time.Duration

data class InstanceProperties(
    val server: ServerProperties,
    val common: CommonProperties,
    val writers: List<OutputWriterProperties>,
    val queries: List<Query>
) {
    data class CommonProperties(
        val period:Duration = Duration.ofSeconds(30),
        val initialDelay:Duration = Duration.ZERO,
        val queryThreads:Int = 2
    )
}

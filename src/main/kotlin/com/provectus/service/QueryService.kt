package com.provectus.service

import com.provectus.model.Query
import com.provectus.writers.OutputWriter
import org.slf4j.LoggerFactory
import java.lang.Exception

class QueryService(
    val jmxConnectionService: JmxConnectionService,
    val outputWriters: List<OutputWriter>
) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(QueryService::class.java)
    }

    fun query(query:Query) {
        try {
            val results = jmxConnectionService.execute(query)
            outputWriters.onEach {
                try {
                    it.write(query, results)
                } catch (e: Exception) {
                    LOGGER.error("Failed to write into ${it.name}.", it, e)
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Failed to execute query {}", query, e)
        }
    }
}
package com.provectus.connectors

import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.IOException
import javax.management.MBeanServerConnection
import javax.management.remote.JMXConnector

data class JMXConnection(val connector:JMXConnector?, val connection: MBeanServerConnection) : Closeable {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(JMXConnection::class.java)
    }

    var isAlive:Boolean = true

    override fun close() {
        try {
            connector?.close()
        } catch (e:IOException) {
            LOGGER.error("Error occurred during close connection {}", this, e)
        }
    }

}
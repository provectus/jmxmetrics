package com.provectus.service

import com.provectus.connectors.JMXConnection
import com.provectus.connectors.JmxConnectionProvider
import com.provectus.connectors.MBeanServerConnectionFactory
import com.provectus.model.Query
import com.provectus.model.ServerProperties
import org.apache.commons.pool.impl.GenericKeyedObjectPool
import org.junit.Test

import org.junit.Assert.*

class JmxConnectionServiceTest {

    private val jmxConnectionServer = JmxConnectionServer(
        ServerProperties(
            local = true
        )
    )
    private val jmxConnectionService = JmxConnectionService(jmxConnectionServer, GenericKeyedObjectPool<JmxConnectionProvider, JMXConnection>(
        MBeanServerConnectionFactory())
    )
    @Test
    fun execute() {
        val results = jmxConnectionService.execute(
            Query(
                objectName = "java.lang:type=GarbageCollector,name=*",
                attr = listOf("CollectionCount", "CollectionTime")
            )
        ).toList()

        assertEquals(4, results.size)
    }
}
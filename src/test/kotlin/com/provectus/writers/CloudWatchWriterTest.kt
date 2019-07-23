package com.provectus.writers

import com.provectus.connectors.JMXConnection
import com.provectus.connectors.JmxConnectionProvider
import com.provectus.connectors.MBeanServerConnectionFactory
import com.provectus.model.Query
import com.provectus.model.ServerProperties
import com.provectus.service.JmxConnectionServer
import com.provectus.service.JmxConnectionService
import com.provectus.service.QueryService
import com.typesafe.config.ConfigFactory
import org.apache.commons.pool.impl.GenericKeyedObjectPool
import org.junit.Assert.*
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.Dimension
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit
import java.util.*

class CloudWatchWriterTest {
    private val jmxConnectionServer = JmxConnectionServer(
        ServerProperties(
            local = true
        )
    )
    private val jmxConnectionService = JmxConnectionService(jmxConnectionServer, GenericKeyedObjectPool<JmxConnectionProvider, JMXConnection>(
        MBeanServerConnectionFactory()
    ))

    private val cloudWatchClient = mock(CloudWatchClient::class.java)

    private val namespace = UUID.randomUUID().toString()
    private val writer = CloudWatchWriter(
        typeNames = listOf("name"),
        settings = ConfigFactory.parseMap(
            mapOf("namespace" to namespace)
        ),
        cloudWatchClient = cloudWatchClient
    )

    private val queryService = QueryService(
        jmxConnectionService,
        listOf(writer)
    )

    @Test
    fun testWriteMetrics() {

        val putMetricDataCaptor = ArgumentCaptor.forClass(PutMetricDataRequest::class.java)
        val containerId = UUID.randomUUID().toString()
        val unit = "Milliseconds"
        val queryNamespace = UUID.randomUUID().toString()

        queryService.query(
            Query(
                objectName = "java.lang:type=GarbageCollector,name=*",
                attr = listOf("CollectionTime"),
                settings = ConfigFactory.parseMap(
                    mapOf(
                        "dimensions" to mapOf(
                            "ContainerId" to containerId
                        ),
                        "unit" to unit,
                        "namespace" to queryNamespace
                    )
                )
            )
        )

        verify(cloudWatchClient).putMetricData(putMetricDataCaptor.capture())
        val request = putMetricDataCaptor.value
        assertTrue(request.metricData().isNotEmpty())
        assertTrue(request.metricData().size == 2)
        assertEquals(queryNamespace, request.namespace())
        for (metricData in request.metricData()) {
            assertEquals(StandardUnit.MILLISECONDS, metricData.unit())
            assertTrue(
                metricData.dimensions().contains(Dimension.builder().name("ContainerId").value(containerId).build())
            )
            assertFalse(
                metricData.dimensions().contains(Dimension.builder().name("type").value("GarbageCollector").build())
            )
            assertNotNull(
                metricData.dimensions().firstOrNull { it.name() == "name"}
            )
        }

    }

}
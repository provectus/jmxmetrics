package com.provectus.service

import com.provectus.connectors.JMXConnection
import com.provectus.connectors.JmxConnectionProvider
import com.provectus.model.Query
import com.provectus.model.Result
import org.apache.commons.pool.KeyedObjectPool
import org.slf4j.LoggerFactory
import java.rmi.UnmarshalException
import java.util.*
import javax.management.MBeanServerConnection
import javax.management.ObjectName

class JmxConnectionService(val server:JmxConnectionServer, val pool: KeyedObjectPool<JmxConnectionProvider, JMXConnection>)  {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(JmxConnectionService::class.java)
    }


    fun execute(query: Query):Iterable<Result> {
        var jmxConnection:JMXConnection? = null

        try {
            jmxConnection = pool.borrowObject(server)
            val connection = jmxConnection.connection

            val result = mutableListOf<Result>()
            for (objectName in queryNames(connection, query.objectName)) {
                result.addAll(fetchResults(connection, query, objectName))
            }
            return result.toList()
        } catch (e:Exception) {
            if (jmxConnection != null) {
                pool.invalidateObject(server, jmxConnection)
                jmxConnection = null
            }
            throw e
        }
        finally {
            if (jmxConnection != null) {
                pool.returnObject(server, jmxConnection)
            }
        }
    }


    private fun queryNames(mbeanServer: MBeanServerConnection, objectName: ObjectName): Iterable<ObjectName> {
        return mbeanServer.queryNames(objectName, null)
    }


    private fun fetchResults(mbeanServer: MBeanServerConnection, query:Query, queryName: ObjectName): Iterable<Result> {
        val oi = mbeanServer.getObjectInstance(queryName)

        val attributes: List<String>

        if (query.attr.isEmpty()) {
            attributes = ArrayList()
            val info = mbeanServer.getMBeanInfo(queryName)
            for (attrInfo in info.attributes) {
                attributes.add(attrInfo.name)
            }
        } else {
            attributes = query.attr
        }

        try {
            if (attributes.isNotEmpty()) {
                LOGGER.debug("Executing queryName [{}] from query [{}]", queryName.canonicalName, this)

                val al = mbeanServer.getAttributes(queryName, attributes.toTypedArray())

                return JmxResultProcessor(query, oi, al.asList(), oi.className, queryName.domain).results
            }
        } catch (ue: UnmarshalException) {
            if (ue.cause != null && ue.cause is ClassNotFoundException) {
                LOGGER.debug(
                    "Bad unmarshal, continuing. This is probably ok and due to something like this: " + "http://ehcache.org/xref/net/sf/ehcache/distribution/RMICacheManagerPeerListener.html#52",
                    ue.message
                )
            } else {
                throw ue
            }
        }

        return emptyList()
    }
}
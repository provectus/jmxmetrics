package com.provectus.connectors

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory
import java.lang.RuntimeException
import java.util.concurrent.Executors

class MBeanServerConnectionFactory : BaseKeyedPoolableObjectFactory<JmxConnectionProvider, JMXConnection>() {

    private val executor = Executors.newCachedThreadPool()

    override fun makeObject(server: JmxConnectionProvider?): JMXConnection {
        if (server!=null) {
            return if (server.isLocal) {
                JMXConnection(null, server.localMBeanServer)
            } else {
                val connection = server.serverConnection
                JMXConnection(connection, connection.mBeanServerConnection)
            }
        } else {
            throw RuntimeException("ServerProperties is null")
        }
    }

    override fun destroyObject(key: JmxConnectionProvider?, obj: JMXConnection?) {
        if (obj?.isAlive == false) {
            return
        }

        obj?.isAlive = false

        executor.submit {
            obj?.close()
        }
    }

    override fun validateObject(key: JmxConnectionProvider?, obj: JMXConnection?): Boolean {
        return obj?.isAlive ?: false
    }
}
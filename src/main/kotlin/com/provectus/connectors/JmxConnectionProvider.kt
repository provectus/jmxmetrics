package com.provectus.connectors

import javax.management.MBeanServer
import javax.management.remote.JMXConnector

interface JmxConnectionProvider {
    val serverConnection: JMXConnector

    val localMBeanServer: MBeanServer

    val isLocal: Boolean
}
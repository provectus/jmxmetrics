package com.provectus.service

import com.provectus.connectors.JMXServiceURLProvider
import com.provectus.connectors.JmxConnectionProvider
import com.provectus.connectors.JmxTransRMIClientSocketFactory
import com.provectus.model.ServerProperties
import java.lang.management.ManagementFactory
import javax.management.MBeanServer
import javax.management.remote.JMXConnector
import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXConnectorServerFactory.PROTOCOL_PROVIDER_PACKAGES
import javax.management.remote.JMXServiceURL
import javax.management.remote.rmi.RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE
import javax.naming.Context.SECURITY_CREDENTIALS
import javax.naming.Context.SECURITY_PRINCIPAL

class JmxConnectionServer(private val properties: ServerProperties) : JmxConnectionProvider {
    companion object {
        private const val FRONT = "service:jmx:rmi:///jndi/rmi://"
        private const val BACK = "/jmxrmi"
        private const val DEFAULT_SOCKET_SO_TIMEOUT_MILLIS = 10000
    }

    override val serverConnection: JMXConnector
        get()  {
            val url = jmxServerUrl
            return JMXConnectorFactory.connect(url, getEnvironment())
        }

    override val localMBeanServer: MBeanServer
        get() = ManagementFactory.getPlatformMBeanServer()
    override val isLocal: Boolean
        get() = properties.local


    val jmxServerUrl:JMXServiceURL
        get() {
            return if (properties.pid != null) {
                JMXServiceURLProvider.extractJMXServiceURLFromPid(properties.pid)
            } else {
                JMXServiceURL(url)
            }
        }

    private val url:String?
        get() {
            return properties.url ?: if (properties.host == null || properties.port == null) {
                null
            } else FRONT + properties.host + ":" + properties.port + BACK
        }


    private fun getEnvironment():Map<String,*> {
        return if (properties.protocolProviderPackages != null && properties.protocolProviderPackages.contains("weblogic")) {

            return if (properties.username != null && properties.password != null) {
                mapOf(
                    PROTOCOL_PROVIDER_PACKAGES to properties.protocolProviderPackages,
                    SECURITY_PRINCIPAL to properties.username,
                    SECURITY_CREDENTIALS to properties.password
                )
            } else {
                emptyMap<String,String>()
            }
        } else {
            val environment = mutableMapOf<String, Any>()
            if (properties.username != null && properties.password!=null) {
                val credentials = arrayOf(properties.username, properties.password)
                environment[JMXConnector.CREDENTIALS] = credentials
            }

            val rmiClientSocketFactory = JmxTransRMIClientSocketFactory(DEFAULT_SOCKET_SO_TIMEOUT_MILLIS, properties.ssl)

            // The following is required when JMX is secured with SSL
            // with com.sun.management.jmxremote.ssl=true
            // as shown in http://docs.oracle.com/javase/8/docs/technotes/guides/management/agent.html#gdfvq
            environment[RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE] = rmiClientSocketFactory
            // The following is required when JNDI Registry is secured with SSL
            // with com.sun.management.jmxremote.registry.ssl=true
            // This property is defined in com.sun.jndi.rmi.registry.RegistryContext.SOCKET_FACTORY
            environment["com.sun.jndi.rmi.factory.socket"] = rmiClientSocketFactory

            environment.toMap()
        }
    }
}
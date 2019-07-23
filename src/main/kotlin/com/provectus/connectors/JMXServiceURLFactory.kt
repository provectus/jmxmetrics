package com.provectus.connectors

import com.sun.tools.attach.VirtualMachine
import java.io.File
import java.io.IOException
import java.lang.Exception
import javax.management.remote.JMXServiceURL

object JMXServiceURLProvider {
    private const val CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress"

    fun extractJMXServiceURLFromPid(pid:String): JMXServiceURL {

        try {
            val vm = VirtualMachine.attach(pid)

            try {
                var connectorAddress: String? = vm.agentProperties.getProperty(CONNECTOR_ADDRESS)

                if (connectorAddress == null) {
                    val agent = vm.systemProperties.getProperty("java.home") +
                            File.separator + "lib" + File.separator + "management-agent.jar"

                    vm.loadAgent(agent)

                    connectorAddress = vm.agentProperties.getProperty(CONNECTOR_ADDRESS)
                }
                return JMXServiceURL(connectorAddress)
            } finally {
                vm.detach()
            }
        } catch (e:Exception) {
            throw IOException(e)
        }
    }
}
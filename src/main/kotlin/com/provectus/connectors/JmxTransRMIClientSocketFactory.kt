package com.provectus.connectors

import java.net.Socket
import java.rmi.server.RMIClientSocketFactory
import java.rmi.server.RMISocketFactory
import javax.rmi.ssl.SslRMIClientSocketFactory

class JmxTransRMIClientSocketFactory(private val timeoutMillis:Int, private val rmiClientSocketFactory: RMIClientSocketFactory) : RMIClientSocketFactory {
    constructor(timeoutMillis: Int, ssl:Boolean) :
            this(timeoutMillis, if (ssl) SslRMIClientSocketFactory() else  RMISocketFactory.getDefaultSocketFactory())

    override fun createSocket(host: String?, port: Int): Socket {
        val socket = rmiClientSocketFactory.createSocket(host, port)
        socket.soTimeout = timeoutMillis
        socket.setSoLinger(false, 0)
        return socket
    }
}
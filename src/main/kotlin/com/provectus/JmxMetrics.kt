package com.provectus

import com.provectus.config.ConfigBuilder
import com.provectus.connectors.JMXConnection
import com.provectus.connectors.JmxConnectionProvider
import com.provectus.connectors.MBeanServerConnectionFactory
import com.provectus.service.JmxConnectionServer
import com.provectus.service.JmxConnectionService
import com.provectus.service.QueryService
import com.provectus.service.SchedulerService
import com.typesafe.config.ConfigFactory
import org.apache.commons.pool.KeyedObjectPool
import org.apache.commons.pool.impl.GenericKeyedObjectPool
import org.koin.Logger.slf4jLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.koin.dsl.onClose


object JmxMetrics {

    @JvmStatic fun main(args: Array<String>) {

        val module = module {
            single { ConfigFactory.defaultApplication() }
            single { ConfigBuilder(get()) }
            single { get<ConfigBuilder>().serverProperties }
            single { get<ConfigBuilder>().poolConfig }
            single {
                GenericKeyedObjectPool<JmxConnectionProvider, JMXConnection>(MBeanServerConnectionFactory(), get<GenericKeyedObjectPool.Config>())
                        as KeyedObjectPool<JmxConnectionProvider, JMXConnection>
            } onClose { it?.close() }

            single { JmxConnectionServer(get()) }
            single { JmxConnectionService(get(), get()) }
            single { QueryService(get(), get<ConfigBuilder>().outputWriters) }
            single {
                SchedulerService(
                    get<ConfigBuilder>().queries,
                    get<ConfigBuilder>().period,
                    get<ConfigBuilder>().initialDelay,
                    get(),
                    get<ConfigBuilder>().queryThreads
                )
            } onClose { it?.close() }
        }

        val koin = startKoin {
            modules(module)
            slf4jLogger(Level.INFO)
        }

        Runtime.getRuntime().addShutdownHook(Thread {
            koin.close()
        })

        try {
            val jmxMetricsApplication = JmxMetricsApplication()
            jmxMetricsApplication.schedulerService.run()
        } finally {

        }
    }
}



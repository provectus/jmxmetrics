package com.provectus.config

import com.provectus.model.InstanceProperties
import com.provectus.model.OutputWriterProperties
import com.provectus.model.Query
import com.provectus.model.ServerProperties
import com.provectus.writers.OutputWriter
import com.provectus.writers.OutputWriterFactory
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.apache.commons.pool.impl.GenericKeyedObjectPool
import java.time.Duration
import kotlin.reflect.full.companionObjectInstance

class ConfigBuilder(config: Config) {

    private val instanceProperties = buildInstanceProperties(config)
    val queries:List<Query> = instanceProperties.queries
    val outputWriters:List<OutputWriter> = buildWriters()
    val serverProperties:ServerProperties = instanceProperties.server


    val period: Duration = instanceProperties.common.period
    val initialDelay: Duration = instanceProperties.common.initialDelay
    val queryThreads = instanceProperties.common.queryThreads
    val poolConfig:GenericKeyedObjectPool.Config = buildPoolConfig(config.getConfig("pool"))


    private fun buildInstanceProperties(config: Config):InstanceProperties {
        return InstanceProperties(
            buildServerProperties(config.getConfig("server")),
            buildCommonProperties(config.getConfig("common")),
            buildListOutputWriters(config.getConfigList("writers")),
            buildListQueries(config.getConfigList("queries"))
        )
    }

    private fun buildListOutputWriters(configList: List<Config>):List<OutputWriterProperties> {
        return configList.map {
            OutputWriterProperties(
                it.getString("className"),
                it.getStringListOrEmpty("typeNames"),
                it.getBooleanOrDefault("debug", false),
                it.getConfigOrEmpty("settings")
            )
        }.toList()
    }

    private fun buildListQueries(configList:List<Config>):List<Query> {
        return configList.map {
            Query(
                it.getString("objectName"),
                it.getStringListOrEmpty("keys"),
                it.getStringListOrEmpty("attr"),
                it.getStringListOrEmpty("typeNames").toSet(),
                it.getStringOrNull("resultAlias"),
                it.getBooleanOrDefault("useObjDomainAsKey", false),
                it.getBooleanOrDefault("allowDottedKeys", false),
                it.getBooleanOrDefault("useAllTypeNames", false),
                it.getConfigOrEmpty("settings")
            )
        }.toList()
    }

    private fun buildCommonProperties(config: Config):InstanceProperties.CommonProperties {
        return InstanceProperties.CommonProperties(
            config.getDuration("period"),
            config.getDuration("initialDelay"),
            config.getInt("queryThreads")
        )
    }

    private fun buildServerProperties(config: Config):ServerProperties {
        return ServerProperties (
            config.getStringOrNull("alias"),
            config.getStringOrNull("pid"),
            config.getStringOrNull("host"),
            config.getIntOrNull("port"),
            config.getStringOrNull("username"),
            config.getStringOrNull("password"),
            config.getStringOrNull("protocolProviderPackages"),
            config.getStringOrNull("url"),
            config.getBooleanOrDefault("local", true),
            config.getBooleanOrDefault("ssl", false)
        )
    }

    private fun buildPoolConfig(config: Config):GenericKeyedObjectPool.Config {
        val poolConfig = GenericKeyedObjectPool.Config()

        poolConfig.maxIdle = config.getInt("maxIdle")
        poolConfig.maxActive = config.getInt("maxActive")
        poolConfig.maxTotal = config.getInt("maxTotal")
        poolConfig.minIdle = config.getInt("minIdle")
        poolConfig.whenExhaustedAction = config.getInt("whenExhaustedAction").toByte()

        poolConfig.maxWait = config.getDuration("maxWait").toMillis()
        poolConfig.timeBetweenEvictionRunsMillis = config.getDuration("timeBetweenEvictionRuns").toMillis()
        poolConfig.minEvictableIdleTimeMillis = config.getDuration("minEvictableIdleTime").toMillis()

        poolConfig.testOnBorrow = config.getBoolean("testOnBorrow")
        poolConfig.testOnReturn = config.getBoolean("testOnReturn")
        poolConfig.testWhileIdle = config.getBoolean("testWhileIdle")
        poolConfig.lifo = config.getBoolean("lifo")

        return poolConfig
    }


    private fun buildWriters():List<OutputWriter> {
        return instanceProperties.writers.map { buildOutputWriter(it) }.toList()
    }

    private fun buildOutputWriter(config:OutputWriterProperties):OutputWriter {

        val className = config.className
        val kClass = Class.forName(className).kotlin
        val companion = kClass.companionObjectInstance
        if (companion is OutputWriterFactory) {
            return companion.create(
                typeNames = config.typeNames,
                debug = config.debug,
                settings = config.settings
            )
        } else {
            throw RuntimeException("Companion object is not inherited from OutputWriterFactory")
        }
    }
}

fun Config.getStringOrNull(path:String):String? {
    return if (this.hasPath(path)) {
        this.getString(path)
    } else {
        null
    }
}

fun Config.getIntOrNull(path:String):Int? {
    return if (this.hasPath(path)) {
        this.getInt(path)
    } else {
        null
    }
}

fun Config.getBooleanOrDefault(path:String, default:Boolean):Boolean {
    return if (this.hasPath(path)) {
        this.getBoolean(path)
    } else {
        default
    }
}

fun Config.getStringListOrEmpty(path:String):List<String> {
    return if (this.hasPath(path)) {
        this.getStringList(path)
    } else {
        emptyList()
    }
}

fun Config.getConfigOrEmpty(path:String):Config {
    return if (this.hasPath(path)) {
        this.getConfig(path)
    } else {
        ConfigFactory.empty()
    }
}
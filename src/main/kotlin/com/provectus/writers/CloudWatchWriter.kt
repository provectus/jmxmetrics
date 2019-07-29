package com.provectus.writers

import com.provectus.model.Query
import com.provectus.model.Result
import com.provectus.model.output.ResultSerializer
import com.provectus.model.output.ToDoubleResultSerializer
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueType
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.Dimension
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest
import java.lang.Double.*


class CloudWatchWriter(
    typeNames:List<String>,
    booleanAsNumber:Boolean = false,
    debug:Boolean = false,
    settings: Config = ConfigFactory.empty(),
    private val resultSerializer: ResultSerializer<Double?> = ToDoubleResultSerializer.DEFAULT,
    val cloudWatchClient:CloudWatchClient = CloudWatchClient.builder().build()
)  : BaseOutputWriter(typeNames, booleanAsNumber, debug, settings) {

    private val namespace:String? = settings.getString(NAMESPACE_CONFIG_FIELD)

    companion object : OutputWriterFactory {
        private const val UNIT_CONFIG_FIELD = "unit"
        private const val NAMESPACE_CONFIG_FIELD = "namespace"
        private const val DIMENSIONS_CONFIG_FIELD = "dimensions"

        override fun create(
            typeNames: List<String>,
            booleanAsNumber: Boolean,
            debug: Boolean,
            settings: Config
        ): OutputWriter {
            return CloudWatchWriter(typeNames, booleanAsNumber, debug, settings)
        }
    }

    override fun internalWrite(query: Query, results: List<Result>) {

        val dimensions = buildDimensions(query)
        val namespace = buildNamespace(query)

        val list = results.mapNotNull {
            val d = resultSerializer.serialize(query, it)

            when {
                d == null -> null
                d.isNaN() -> null
                d.isInfinite() -> null
                else -> buildMetric(query, it, d, dimensions)
            }
        }

        if (list.isNotEmpty()) {
            list.chunked(20).onEach {
                val request = PutMetricDataRequest.builder()
                    .namespace(namespace)
                    .metricData(it)
                    .build()

                cloudWatchClient.putMetricData(request)
            }
        }
    }

    private fun buildMetric(query:Query, result:Result,value:Double, dimensions:List<Dimension>):MetricDatum {
        val mb = MetricDatum.builder()
        val datum:MetricDatum.Builder = mb.metricName(result.attributeName)
            .value(value)
            .dimensions(dimensions + typeNamesToDimension(filterTypeNames(result.typeNameMap)))
        if (query.settings.hasPath(UNIT_CONFIG_FIELD) &&
            query.settings.getValue(UNIT_CONFIG_FIELD).valueType() == ConfigValueType.STRING
        ) {
            datum.unit(query.settings.getString(UNIT_CONFIG_FIELD))
        }
        return datum.build()
    }

    private fun buildNamespace(query: Query): String? {
        return if (query.settings.hasPath(NAMESPACE_CONFIG_FIELD)) {
            query.settings.getString(NAMESPACE_CONFIG_FIELD)
        } else {
            this.namespace
        }
    }

    private fun typeNamesToDimension(typeNames: Map<String,String>):List<Dimension> {
        return typeNames.map {
            Dimension.builder()
                .name(it.key)
                .value(it.value)
                .build()
        }.toList()
    }


    private fun buildDimensions(query:Query):List<Dimension> {
        return if (query.settings.hasPath(DIMENSIONS_CONFIG_FIELD)) {
            val dimensions = query.settings.getConfig(DIMENSIONS_CONFIG_FIELD)
            dimensions.entrySet()
                .filter { it.value.valueType() == ConfigValueType.STRING }
                .map {
                    Dimension.builder()
                        .name(it.key)
                        .value(dimensions.getString(it.key))
                        .build()
                }.toList()
        } else {
            emptyList()
        }

    }

    override val name: String
        get() = CloudWatchWriter::class.java.name

    override fun close() {
        cloudWatchClient.close()
    }


}
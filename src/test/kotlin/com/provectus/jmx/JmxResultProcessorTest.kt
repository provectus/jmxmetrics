package com.provectus.jmx

import com.provectus.model.Query
import com.provectus.model.Result
import com.provectus.service.JmxResultProcessor
import org.junit.Assume.assumeNoException
import org.junit.Test
import java.lang.management.ManagementFactory
import java.util.*
import javax.management.*
import javax.management.openmbean.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JmxResultProcessorTest {

    companion object {
        private const val TEST_DOMAIN_NAME = "ObjectDomainName"

        fun dummyQueryWithResultAlias(): Query {
            return Query(objectName = "myQuery:key=val", resultAlias = "resultAlias")
        }
    }


    private val runtime: ObjectInstance
        @Throws(MalformedObjectNameException::class, InstanceNotFoundException::class)
        get() = ManagementFactory.getPlatformMBeanServer().getObjectInstance(
            ObjectName("java.lang", "type", "Runtime")
        )

    private val memory: ObjectInstance
        @Throws(MalformedObjectNameException::class, InstanceNotFoundException::class)
        get() = ManagementFactory.getPlatformMBeanServer().getObjectInstance(
            ObjectName("java.lang", "type", "Memory")
        )

    private val g1YoungGen: ObjectInstance
        @Throws(MalformedObjectNameException::class, InstanceNotFoundException::class)
        get() {
            val keyProperties = Hashtable<String, String>()
            keyProperties["type"] = "GarbageCollector"
            keyProperties["name"] = "G1 Young Generation"
            return ManagementFactory.getPlatformMBeanServer().getObjectInstance(
                ObjectName("java.lang", keyProperties)
            )
        }

    private val compositeData: CompositeData
        @Throws(OpenDataException::class)
        get() {
            val keys = arrayOf("p1")
            val itemTypes = arrayOf<OpenType<*>>(SimpleType.STRING)
            val compType = CompositeType("compType", "descr", keys, keys, itemTypes)
            val values = mapOf<String,Any?>("p1" to null)
            return CompositeDataSupport(compType, values)
        }

    @Test
    @Throws(MalformedObjectNameException::class, InstanceNotFoundException::class)
    fun canCreateBasicResultData() {
        val integerAttribute = Attribute("StartTime", 51L)
        val runtime = runtime
        val results = JmxResultProcessor(
            dummyQueryWithResultAlias(),
            runtime,
            listOf(integerAttribute),
            runtime.className,
            TEST_DOMAIN_NAME
        ).results

        assertEquals(1, results.size)
        val integerResult = results[0]

        assertEquals("StartTime", integerResult.attributeName)
        assertEquals("sun.management.RuntimeImpl", integerResult.className)
        assertEquals("resultAlias", integerResult.keyAlias)
        assertEquals("type=Runtime", integerResult.typeName)
        assertEquals(51L, integerResult.value)

    }

    @Test
    @Throws(MalformedObjectNameException::class)
    fun doesNotReorderTypeNames() {
        val className = "java.lang.SomeClass"
        val propertiesOutOfOrder = "z-key=z-value,a-key=a-value,k-key=k-value"
        val results = JmxResultProcessor(
            dummyQueryWithResultAlias(),
            ObjectInstance("$className:$propertiesOutOfOrder", className),
            listOf(Attribute("SomeAttribute", 1)),
            className,
            TEST_DOMAIN_NAME
        ).results

        assertEquals(1, results.size)
        val integerResult = results[0]
        assertEquals(propertiesOutOfOrder, integerResult.typeName)

    }

    @Test
    @Throws(MalformedObjectNameException::class, OpenDataException::class, InstanceNotFoundException::class)
    fun testNullValueInCompositeData() {
        val runtime = runtime
        val results = JmxResultProcessor(
            dummyQueryWithResultAlias(),
            runtime,
            listOf(Attribute("SomeAttribute", compositeData)),
            runtime.className,
            TEST_DOMAIN_NAME
        ).results

        assertEquals(0, results.size)
    }

    @Test
    @Throws(MalformedObjectNameException::class, InstanceNotFoundException::class)
    fun canReadSingleIntegerValue() {
        val integerAttribute = Attribute("CollectionCount", 51L)
        val runtime = runtime
        val results = JmxResultProcessor(
            dummyQueryWithResultAlias(),
            runtime,
            listOf(integerAttribute),
            runtime.className,
            TEST_DOMAIN_NAME
        ).results

        assertEquals(1, results.size)
        val integerResult = results[0]

        assertEquals("CollectionCount", integerResult.attributeName)
        assertTrue(integerResult.value is Long)
        assertEquals(51L, integerResult.value)
    }

    @Test
    @Throws(MalformedObjectNameException::class, InstanceNotFoundException::class)
    fun canReadSingleBooleanValue() {
        val booleanAttribute = Attribute("BootClassPathSupported", true)
        val runtime = runtime
        val results = JmxResultProcessor(
            dummyQueryWithResultAlias(),
            runtime,
            listOf(booleanAttribute),
            runtime.className,
            TEST_DOMAIN_NAME
        ).results

        assertEquals(1, results.size)
        val result = results[0]

        assertEquals("BootClassPathSupported", result.attributeName)
        assertTrue(result.value is Boolean)
        assertEquals(true, result.value)
    }

    private fun firstMatch(results: List<Result>, attributeName: String, vararg valuePath: String): Result? {
        return results.firstOrNull { it.attributeName == attributeName && it.valuePath == valuePath.toList() }
    }


    @Test
    @Throws(
        MalformedObjectNameException::class,
        AttributeNotFoundException::class,
        MBeanException::class,
        ReflectionException::class,
        InstanceNotFoundException::class
    )
    fun canReadTabularData() {
        val runtime = runtime
        val attr = ManagementFactory.getPlatformMBeanServer().getAttributes(
            runtime.objectName, arrayOf("SystemProperties")
        )
        val results = JmxResultProcessor(
            dummyQueryWithResultAlias(),
            runtime,
            attr.asList(),
            runtime.className,
            TEST_DOMAIN_NAME
        ).results

        assertTrue(results.size > 2)

        val result = firstMatch(results, "SystemProperties", "java.version", "value")
        assertNotNull(result)
        assertEquals("SystemProperties", result.attributeName)
        assertEquals(listOf("java.version", "value"), result.valuePath)
        assertEquals(System.getProperty("java.version"), result.value)
    }


    @Test(timeout = 1000)
    @Throws(
        MalformedObjectNameException::class,
        AttributeNotFoundException::class,
        MBeanException::class,
        ReflectionException::class,
        InstanceNotFoundException::class
    )
    fun canReadFieldsOfTabularData() {
        // Need to induce a GC for the attribute below to be populated
        Runtime.getRuntime().gc()

        var runtime: ObjectInstance? = null
        try {
            runtime = g1YoungGen
        } catch (e: InstanceNotFoundException) {
            // ignore test if G1 not enabled
            assumeNoException("G1 GC in Java 7/8 needs to be enabled with -XX:+UseG1GC", e)
        }

        var attr: AttributeList
        // but takes a non-deterministic amount of time for LastGcInfo to get populated
        while (true) { // but bounded by Test timeout
            attr = ManagementFactory.getPlatformMBeanServer().getAttributes(
                runtime!!.objectName, arrayOf("LastGcInfo")
            )
            if ((attr[0] as Attribute).value != null) {
                break
            }
        }

        val results = JmxResultProcessor(
            dummyQueryWithResultAlias(),
            runtime,
            attr.asList(),
            runtime.className,
            TEST_DOMAIN_NAME
        ).results

        assertTrue(results.size > 2)

        // Should have primitive typed fields
        assertNotNull(firstMatch(results, "LastGcInfo", "duration"))
        // assert tabular fields are excluded
        assertNull(firstMatch(results, "LastGcInfo", "memoryUsageBeforeGc"))
        assertNull(firstMatch(results, "LastGcInfo", "memoryUsageAfterGc"))
    }

    @Test
    @Throws(
        MalformedObjectNameException::class,
        AttributeNotFoundException::class,
        MBeanException::class,
        ReflectionException::class,
        InstanceNotFoundException::class
    )
    fun canReadCompositeData() {
        val memory = memory
        val attr = ManagementFactory.getPlatformMBeanServer().getAttributes(
            memory.objectName, arrayOf("HeapMemoryUsage")
        )
        val results = JmxResultProcessor(
            dummyQueryWithResultAlias(),
            memory,
            attr.asList(),
            memory.className,
            TEST_DOMAIN_NAME
        ).results

        assertEquals(results.size, 4)

        for (result in results) {
            assertEquals("HeapMemoryUsage", result.attributeName)
            assertEquals("type=Memory", result.typeName)
        }

        val result = firstMatch(results, "HeapMemoryUsage", "init")
        assertNotNull(result)
        assertTrue(result.value is Long)
    }


    @Test
    @Throws(
        MalformedObjectNameException::class,
        AttributeNotFoundException::class,
        MBeanException::class,
        ReflectionException::class,
        InstanceNotFoundException::class
    )
    fun canReadCompositeDataWithFilteringKeys() {
        val memory = memory
        val attr = ManagementFactory.getPlatformMBeanServer().getAttributes(
            memory.objectName, arrayOf("HeapMemoryUsage")
        )
        val results = JmxResultProcessor(
            Query(
                objectName = "myQuery:key=val",
                resultAlias = "resultAlias",
                keys = listOf("init")
            ),
            memory,
            attr.asList(),
            memory.className,
            TEST_DOMAIN_NAME
        ).results

        assertEquals(1, results.size)

        for (result in results) {
            assertEquals("HeapMemoryUsage", result.attributeName)
            assertEquals("type=Memory", result.typeName)
        }


        val result = firstMatch(results, "HeapMemoryUsage", "init")
        assertNotNull(result)
        assertTrue(result.value is Long)
    }

    @Test
    @Throws(MalformedObjectNameException::class)
    fun canReadMapData() {
        val mapAttribute = Attribute("map", mapOf("key1" to "value1", "key2" to "value2"))

        val results = JmxResultProcessor(
            dummyQueryWithResultAlias(),
            ObjectInstance("java.lang:type=Memory", "java.lang.SomeClass"),
            listOf(mapAttribute),
            "java.lang.SomeClass",
            TEST_DOMAIN_NAME
        ).results

        assertNotNull(results)
        assertEquals(2, results.size)

        for (result in results) {
            assertEquals("map", result.attributeName)
            assertEquals("type=Memory", result.typeName)
        }

        assertEquals("value1", firstMatch(results, "map", "key1")?.value)
        assertEquals("value2", firstMatch(results, "map", "key2")?.value)
    }

    @Test
    @Throws(MalformedObjectNameException::class)
    fun canReadMapDataWithNonStringKeys() {
        val mapAttribute = Attribute("map", mapOf(1 to "value1", 2 to "value2"))

        val results = JmxResultProcessor(
            dummyQueryWithResultAlias(),
            ObjectInstance("java.lang:type=Memory", "java.lang.SomeClass"),
            listOf(mapAttribute),
            "java.lang.SomeClass",
            TEST_DOMAIN_NAME
        ).results

        assertNotNull(results)
        for (result in results) {
            assertEquals("map", result.attributeName)
            assertEquals("type=Memory", result.typeName)
        }

        assertEquals("value1", firstMatch(results, "map", "1")?.value)
        assertEquals("value2", firstMatch(results, "map", "2")?.value)
    }
}
package com.provectus.model

import com.provectus.model.typename.TypeNameValue
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TypeNameValueTest {

    private fun assertExtractsTypeNameValues(typeNameStr: String, vararg expectedValues: TypeNameValue) {

        val expected = expectedValues.toList()
        val actual = TypeNameValue.extract(typeNameStr).toList()
        assertTrue("Expected <$expected>, actual <$actual> is not same.") { actual == expected }
    }

    @Test
    fun testExtractsEmpty() {
        assertExtractsTypeNameValues("")
        assertExtractsTypeNameValues(",,,")
    }

    @Test
    fun testExtractsSingle() {
        assertExtractsTypeNameValues("key=value", TypeNameValue("key", "value"))
    }

    @Test
    fun testExtractsMultiple() {
        assertExtractsTypeNameValues(
            "key1=value1,key2=value2",
            TypeNameValue("key1", "value1"),
            TypeNameValue("key2", "value2")
        )
        // reorder
        assertExtractsTypeNameValues(
            "key2=value2,key1=value1",
            TypeNameValue("key2", "value2"),
            TypeNameValue("key1", "value1")
        )
    }

    @Test
    fun testExtractsWithoutValue() {
        assertExtractsTypeNameValues(
            "key",
            TypeNameValue("key")
        )
        assertExtractsTypeNameValues(
            "key1,key2",
            TypeNameValue("key1"),
            TypeNameValue("key2")
        )
    }

    @Test
    fun testExtractsMixedCases() {
        assertExtractsTypeNameValues(
            "key1=value1,,key2=value2,key3",
            TypeNameValue("key1", "value1"),
            TypeNameValue("key2", "value2"),
            TypeNameValue("key3")
        )
        assertExtractsTypeNameValues(
            "key1=value1,key2,,key3=value3",
            TypeNameValue("key1", "value1"),
            TypeNameValue("key2"),
            TypeNameValue("key3", "value3")
        )
        assertExtractsTypeNameValues(
            ",key1,key2=value2,key3=value3",
            TypeNameValue("key1"),
            TypeNameValue("key2", "value2"),
            TypeNameValue("key3", "value3")
        )
        assertExtractsTypeNameValues(
            "key1,key2=value2,key3,",
            TypeNameValue("key1"),
            TypeNameValue("key2", "value2"),
            TypeNameValue("key3")
        )
    }

    @Test
    fun testExtractMap() {
        assertTrue(TypeNameValue.extractMap(null).isEmpty())
        assertTrue(TypeNameValue.extractMap("").isEmpty())
        assertEquals(mapOf("x-key1-x" to ""), TypeNameValue.extractMap("x-key1-x"))
        assertEquals(mapOf("x-key1-x" to "", "x-key2-x" to ""), TypeNameValue.extractMap("x-key1-x,x-key2-x"))
        assertEquals(mapOf("x-key1-x" to "x-value1-x"), TypeNameValue.extractMap("x-key1-x=x-value1-x"))
        assertEquals(mapOf("x-key1-x" to "x-value1-x", "y-key2-y" to "y-value2-y"),
            TypeNameValue.extractMap("x-key1-x=x-value1-x,y-key2-y=y-value2-y")
            )
        assertEquals(
            mapOf("x-key1-x" to  "x-value1-x", "y-key2-y" to  "y-value2-y", "z-key3-z" to "z-value3-z"),
            TypeNameValue.extractMap("x-key1-x=x-value1-x,y-key2-y=y-value2-y,z-key3-z=z-value3-z")
        )
        assertEquals(
            mapOf("x-key1-x" to "x-value1-x",
                "y-key2-y" to "",
                "yy-key2.5-yy" to "a=1",
                "z-key3-z" to "z-value3-z"),
            TypeNameValue.extractMap("x-key1-x=x-value1-x,y-key2-y,yy-key2.5-yy=a=1,z-key3-z=z-value3-z")
        )
    }
}
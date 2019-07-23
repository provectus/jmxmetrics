package com.provectus.model

import org.junit.Test
import kotlin.test.assertEquals

class QueryTests {

    @Test
    fun testBuilderTypeNameValueStringFooBar() {
        val typeNames = setOf("foo", "bar")
        val typeNameStr = "foo=FOO,bar=BAR,baz=BAZ"

        val query = Query(objectName = "obj:$typeNameStr", typeNames = typeNames)
        val actual = query.makeTypeNameValueString(typeNames, typeNameStr)
        assertEquals("FOO_BAR", actual)
    }

    @Test
    fun testBuilderTypeNameValueStringBarFoo() {
        val typeNames = setOf("bar", "foo")
        val typeNameStr = "foo=FOO,bar=BAR,baz=BAZ"
        val query = Query(objectName ="obj:$typeNameStr", typeNames = typeNames)

        val actual = query.makeTypeNameValueString(typeNames, typeNameStr)
        assertEquals("BAR_FOO", actual)
    }
}
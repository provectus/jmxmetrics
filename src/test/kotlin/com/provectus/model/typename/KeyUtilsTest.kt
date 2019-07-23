package com.provectus.model.typename

import com.provectus.jmx.JmxResultProcessorTest.Companion.dummyQueryWithResultAlias
import com.provectus.model.Query
import org.junit.Test
import com.provectus.model.Result
import kotlin.test.assertEquals

class KeyUtilsTest {

    private fun dummyQuery(): Query {
        return Query(objectName = "myQuery:key=val")
    }

    private fun numericResult(): Result {
        return numericResult(10)
    }

    private fun numericResult(numericValue: Any): Result {
        return numericResult("MemoryAlias", numericValue)
    }

    private fun numericResult(keyAlias: String, numericValue: Any): Result {
        return Result(
            "ObjectPendingFinalizationCount",
            "sun.management.MemoryImpl",
            "ObjectDomainName",
            "type=Memory",
            emptyList(),
            numericValue,
            0,
            keyAlias
        )
    }


    @Test
    fun testKeyString() {
        assertEquals(
            "rootPrefix.MemoryAlias.ObjectPendingFinalizationCount",
            KeyUtils.getKeyString(
                dummyQueryWithResultAlias(),
                numericResult(),
                listOf("typeName"),
                "rootPrefix"
            )
        )
        assertEquals(
            "rootPrefix.ObjectPendingFinalizationCount",
            KeyUtils.getKeyString(
                dummyQuery(),
                numericResult("", 10),
                listOf("typeName"),
                "rootPrefix"
            )
        )
        assertEquals(
            "MemoryAlias.ObjectPendingFinalizationCount",
            KeyUtils.getKeyString(
                dummyQuery(),
                numericResult(10),
                listOf("typeName"),
                ""
            )
        )
        assertEquals(
            "ObjectPendingFinalizationCount",
            KeyUtils.getKeyString(
                dummyQuery(),
                numericResult("", 10),
                listOf("typeName"),
                ""
            )
        )
        assertEquals(
            "ObjectPendingFinalizationCount",
            KeyUtils.getKeyString(
                dummyQuery(),
                numericResult("", 10),
                listOf("typeName"),
                ""
            )
        )
    }
}
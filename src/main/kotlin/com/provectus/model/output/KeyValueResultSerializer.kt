package com.provectus.model.output

import com.provectus.model.Query
import com.provectus.model.Result
import com.provectus.model.typename.KeyUtils

class KeyValueResultSerializer(format: String?, private val typeNames: List<String>) : ResultSerializer<String> {
    private val format: String

    init {
        this.format = format ?: DEFAULT_FORMAT
    }

    override fun serialize(query: Query, result: Result): String {
        return String.format(format, KeyUtils.getKeyString(query, result, typeNames), result.value)
    }

    companion object {
        private const val DEFAULT_FORMAT = "%s=%s"

        fun createDefault(typeNames: List<String>): KeyValueResultSerializer {
            return KeyValueResultSerializer(DEFAULT_FORMAT, typeNames)
        }
    }
}
package com.provectus.model.output

import com.provectus.model.Query
import com.provectus.model.Result
import com.provectus.utils.NumberUtils

class ToDoubleResultSerializer : ResultSerializer<Double?> {

    override fun serialize(query: Query, result: Result): Double? {
        return if (NumberUtils.isNumeric(result.value)) {
            when (val v = result.value) {
                is Number -> v.toDouble()
                is String -> v.toDoubleOrNull()
                else -> null
            }
        } else {
            null
        }
    }

    companion object {
        val DEFAULT = ToDoubleResultSerializer()
    }

}
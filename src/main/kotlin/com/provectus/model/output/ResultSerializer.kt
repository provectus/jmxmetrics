package com.provectus.model.output

import com.provectus.model.Query
import com.provectus.model.Result

interface ResultSerializer<T> {
    fun serialize(query: Query, result: Result): T
}
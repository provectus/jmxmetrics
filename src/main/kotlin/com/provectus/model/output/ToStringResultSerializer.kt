package com.provectus.model.output

import com.provectus.model.Query
import com.provectus.model.Result

class ToStringResultSerializer @JvmOverloads constructor(
    /**
     * Flag to show Server and Query info as well
     */
    private val verbose: Boolean = false
) : ResultSerializer<String> {

    override fun serialize(query: Query, result: Result): String {
        return if (verbose) {
            "$query $result"
        } else result.toString()
    }

    companion object {
        val DEFAULT = ToStringResultSerializer()
    }

}
package com.provectus.model.typename

data class TypeNameValue(val key:String, val value:String = "") {
    companion object {
        fun extractMap(typeNameStr:String?):Map<String,String> {
            return if (typeNameStr != null) {
                return extract(typeNameStr).map {
                    Pair(it.key,it.value)
                }.toMap()
            } else {
                mapOf()
            }
        }

        fun extract(typeNameStr: String): Iterable<TypeNameValue> {
            return object : Iterable<TypeNameValue> {
                override fun iterator(): Iterator<TypeNameValue> {
                    return TypeNameValuesIterator(typeNameStr)
                }
            }
        }
    }
}

class TypeNameValuesIterator(typeNameStr:String) : Iterator<TypeNameValue> {
    private val tokens = typeNameStr.split(',')
        .dropWhile { it.isEmpty() }
        .dropLastWhile { it.isEmpty() }
    private var iterator: Int = 0

    override fun hasNext(): Boolean {
        return iterator < tokens.size
    }

    override fun next(): TypeNameValue {
        val keyVal = tokens[iterator].split('=', limit = 2)
        val result = if (keyVal.size > 1) {
            TypeNameValue(keyVal[0], keyVal[1])
        } else {
            TypeNameValue(keyVal[0])
        }
        ++iterator
        skipEmpty()
        return result
    }

    private fun skipEmpty() {
        while (iterator < tokens.size && tokens[iterator].isEmpty()) {
            ++iterator
        }
    }
}
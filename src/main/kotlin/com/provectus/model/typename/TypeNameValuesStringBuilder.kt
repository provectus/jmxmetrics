package com.provectus.model.typename

open class TypeNameValuesStringBuilder(val separator: String = DEFAULT_SEPARATOR) {

    open fun build(typeNames: Set<String>, typeNameStr: String): String? {
        return doBuild(typeNames, typeNameStr)
    }

    protected fun doBuild(typeNames: Set<String>, typeNameStr: String): String? {
        if (typeNames.isEmpty()) {
            return null
        }
        val typeNameValueMap = TypeNameValue.extractMap(typeNameStr)
        val sb = StringBuilder()
        for (key in typeNames) {
            val result = typeNameValueMap[key]
            if (result != null) {
                sb.append(result)
                sb.append(separator)
            }
        }
        return StringUtils.chomp(sb.toString(), separator)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TypeNameValuesStringBuilder

        if (separator != other.separator) return false

        return true
    }

    override fun hashCode(): Int {
        return separator.hashCode()
    }

    override fun toString(): String {
        return "TypeNameValuesStringBuilder(separator='$separator')"
    }


    companion object {

        const val DEFAULT_SEPARATOR = "_"
        val DEFAULT_BUILDER = TypeNameValuesStringBuilder()

    }



}
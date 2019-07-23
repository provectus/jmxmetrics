package com.provectus.model.typename

class UseAllTypeNameValuesStringBuilder(separator: String) : TypeNameValuesStringBuilder(separator) {

    override fun build(typeNames: Set<String>, typeNameStr: String): String? {
        val allTypeNames = mutableSetOf<String>()
        for (typeNameValue in TypeNameValue.extract(typeNameStr)) {
            if (typeNameValue.value.isNotEmpty()) {
                allTypeNames.add(typeNameValue.key)
            }
        }
        return doBuild(allTypeNames, typeNameStr)
    }

    override fun toString(): String {
        return "UseAllTypeNameValuesStringBuilder(separator='$separator')"
    }


}
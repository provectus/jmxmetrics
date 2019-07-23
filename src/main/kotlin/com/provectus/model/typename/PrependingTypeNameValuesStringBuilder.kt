package com.provectus.model.typename

class PrependingTypeNameValuesStringBuilder(separator: String, prependedTypeNames: List<String>) :
    TypeNameValuesStringBuilder(separator) {

    private val prependedTypeNames: Set<String> = prependedTypeNames.toSet()

    override fun build(typeNames: Set<String>, typeNameStr: String): String? {
        val resultingTypeNames = prependedTypeNames.toMutableSet()
        for (name in typeNames) {
            if (!resultingTypeNames.contains(name)) {
                resultingTypeNames.add(name)
            }
        }
        return doBuild(resultingTypeNames, typeNameStr)
    }

    override fun toString(): String {
        return "PrependingTypeNameValuesStringBuilder(separator='$separator', prependedTypeNames=$prependedTypeNames)}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as PrependingTypeNameValuesStringBuilder

        if (prependedTypeNames != other.prependedTypeNames) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + prependedTypeNames.hashCode()
        return result
    }


}
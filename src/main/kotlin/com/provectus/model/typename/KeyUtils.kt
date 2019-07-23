package com.provectus.model.typename

import com.provectus.model.Query
import com.provectus.model.Result

object KeyUtils {
    fun getKeyString(query: Query, result: Result, typeNames: List<String>, rootPrefix:String? = null): String {
        val sb = StringBuilder()
        if (rootPrefix != null) {
            addRootPrefix(rootPrefix, sb)
            addSeparator(sb)
        }
        addMBeanIdentifier(query, result, sb)
        addSeparator(sb)
        addTypeName(query, result, typeNames, sb)
        addKeyString(query, result, sb)
        return sb.toString()
    }

    private fun addMBeanIdentifier(query: Query, result: Result, sb: StringBuilder) {
        when {
            result.keyAlias != null -> sb.append(result.keyAlias)
            query.useObjDomainAsKey -> sb.append(StringUtils.cleanupStr(result.objDomain, query.allowDottedKeys))
            else -> sb.append(StringUtils.cleanupStr(result.className))
        }
    }

    private fun addTypeName(query: Query, result: Result, typeNames: List<String>, sb: StringBuilder) {
        val typeName = StringUtils.cleanupStr(
            query.makeTypeNameValueString(typeNames.toSet(), result.typeName),
            query.allowDottedKeys
        )
        if (typeName != null && typeName.isNotEmpty()) {
            sb.append(typeName)
            sb.append(".")
        }
    }

    private fun addRootPrefix(rootPrefix: String?, sb: StringBuilder) {
        if (rootPrefix != null && rootPrefix.isNotEmpty()) {
            sb.append(rootPrefix)
            sb.append(".")
        }
    }


    private fun addSeparator(sb: StringBuilder) {
        if (sb.isNotEmpty() && sb.substring(sb.length - 1) != ".") {
            sb.append(".")
        }
    }

    private fun addKeyString(query: Query, result: Result, sb: StringBuilder) {
        val keyStr = getValueKey(result)
        sb.append(StringUtils.cleanupStr(keyStr, query.allowDottedKeys))
    }

    private fun getValueKey(result: Result): String {
        return if (result.valuePath.isEmpty()) {
            result.attributeName
        } else result.attributeName + "." + getValuePathString(result)
    }

    private fun getValuePathString(result: Result): String {
        return result.valuePath.joinToString(".")
    }


}
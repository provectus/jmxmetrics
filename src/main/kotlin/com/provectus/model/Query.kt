package com.provectus.model

import com.provectus.model.typename.PrependingTypeNameValuesStringBuilder
import com.provectus.model.typename.TypeNameValuesStringBuilder
import com.provectus.model.typename.UseAllTypeNameValuesStringBuilder
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.Serializable
import javax.management.ObjectName

data class Query(
    val objectName:ObjectName,
    val keys:List<String> = emptyList(),
    val attr:List<String> = emptyList(),
    val typeNames:Set<String> = emptySet(),
    val resultAlias:String? = null,
    val useObjDomainAsKey:Boolean = false,
    val allowDottedKeys:Boolean = false,
    val useAllTypeNames:Boolean = false,
    val settings:Config = ConfigFactory.empty()
) : Serializable {

    constructor(
        objectName:String,
        keys:List<String> = emptyList(),
        attr:List<String> = emptyList(),
        typeNames:Set<String> = emptySet(),
        resultAlias:String? = null,
        useObjDomainAsKey:Boolean = false,
        allowDottedKeys:Boolean = false,
        useAllTypeNames:Boolean = false,
        settings:Config = ConfigFactory.empty()
    ) : this(ObjectName(objectName),keys,attr,typeNames,resultAlias,useObjDomainAsKey,allowDottedKeys,useAllTypeNames,settings)

    private val typeNameValuesStringBuilder = makeTypeNameValuesStringBuilder()

    fun makeTypeNameValueString(typeNames: Set<String>, typeNameStr: String): String? {
        return this.typeNameValuesStringBuilder.build(typeNames, typeNameStr)
    }

    private fun makeTypeNameValuesStringBuilder(): TypeNameValuesStringBuilder {
        val separator = if (allowDottedKeys) "." else TypeNameValuesStringBuilder.DEFAULT_SEPARATOR
        return when {
            useAllTypeNames -> UseAllTypeNameValuesStringBuilder(separator)
            typeNames.isNotEmpty() -> PrependingTypeNameValuesStringBuilder(separator, ArrayList<String>(typeNames))
            else -> TypeNameValuesStringBuilder(separator)
        }
    }

}


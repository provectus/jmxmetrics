package com.provectus.model

import com.provectus.model.typename.TypeNameValue

data class Result(
    val attributeName:String,
    val className:String,
    val objDomain:String,
    val typeName:String,
    val valuePath:List<String>,
    val value:Any?,
    val epoch:Long,
    val keyAlias:String?
) {
    val typeNameMap: Map<String,String>
        get() = TypeNameValue.extractMap(this.typeName)
}
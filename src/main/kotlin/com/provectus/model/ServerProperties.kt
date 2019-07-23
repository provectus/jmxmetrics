package com.provectus.model

data class ServerProperties(
    val alias:String? = null,
    val pid:String? = null,
    val host:String? = null,
    val port:Int? = null,
    val username:String? = null,
    val password:String? = null,
    val protocolProviderPackages:String? = null,
    val url:String? = null,
    val local:Boolean = true,
    val ssl: Boolean = false
)
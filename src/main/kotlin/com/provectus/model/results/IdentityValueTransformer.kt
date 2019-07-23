package com.provectus.model.results

class IdentityValueTransformer : ValueTransformer {
    override fun apply(t: Any?): Any? {
        return t
    }
}